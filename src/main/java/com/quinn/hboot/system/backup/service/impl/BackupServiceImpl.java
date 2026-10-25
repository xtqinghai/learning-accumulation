package com.quinn.hboot.system.backup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.system.backup.mapper.BackupRecordMapper;
import com.quinn.hboot.system.backup.model.entity.BackupRecord;
import com.quinn.hboot.system.backup.model.query.BackupRecordQuery;
import com.quinn.hboot.system.backup.model.vo.BackupRecordVO;
import com.quinn.hboot.system.backup.service.BackupService;
import com.quinn.hboot.system.config.BackupProperties;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BackupServiceImpl implements BackupService {

    private static final int RUNNING = 0;
    private static final int SUCCESS = 1;
    private static final int FAILED = 2;
    private static final int NOT_VERIFIED = 0;
    private static final int VERIFIED = 1;
    private static final int MAX_MANIFEST_FILES = 10_000;
    private static final DateTimeFormatter NAME_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final BackupRecordMapper backupRecordMapper;
    private final BackupProperties backupProperties;
    private final ObjectMapper objectMapper;

    @Value("${spring.datasource.dynamic.datasource.hboot.url}")
    private String databaseUrl;

    @Value("${spring.datasource.dynamic.datasource.hboot.username}")
    private String databaseUsername;

    @Value("${spring.datasource.dynamic.datasource.hboot.password:}")
    private String databasePassword;

    @Value("${file.local.base-path}")
    private String fileBasePath;

    @Override
    public IPage<BackupRecordVO> page(BackupRecordQuery query) {
        Page<BackupRecord> page = backupRecordMapper.selectPage(query.buildPage(), new LambdaQueryWrapper<BackupRecord>()
            .like(StringUtils.hasText(query.getBackupName()), BackupRecord::getBackupName, query.getBackupName())
            .eq(query.getStatus() != null, BackupRecord::getStatus, query.getStatus())
            .orderByDesc(BackupRecord::getCreateTime)
            .orderByDesc(BackupRecord::getId));
        return page.convert(this::toVO);
    }

    @Override
    public BackupRecordVO get(Long id) {
        return toVO(require(id));
    }

    @Override
    public Long createDatabaseBackup() {
        DatabaseTarget target = databaseTarget();
        Path directory = backupDirectory();
        String baseName = "hboot-postgresql-" + NAME_TIME.format(LocalDateTime.now()) + "-" + UUID.randomUUID();
        Path backupPath = directory.resolve(baseName + ".dump");
        Path partialPath = directory.resolve(baseName + ".partial");
        Path manifestPath = directory.resolve(baseName + ".manifest.json");
        BackupRecord record = new BackupRecord();
        record.setBackupName(baseName);
        record.setBackupType("POSTGRESQL");
        record.setStatus(RUNNING);
        record.setVerifyStatus(NOT_VERIFIED);
        record.setFileName(backupPath.getFileName().toString());
        record.setFilePath(backupPath.toString());
        record.setManifestPath(manifestPath.toString());
        record.setCreator(SecurityUtils.getUsername());
        record.setStartTime(LocalDateTime.now());
        record.setCreateTime(record.getStartTime());
        record.setUpdateTime(record.getStartTime());
        backupRecordMapper.insert(record);

        try {
            ProcessResult dumpResult = runProcess(dumpCommand(target, partialPath), directory, "dump");
            if (dumpResult.exitCode() != 0 || !Files.isRegularFile(partialPath) || Files.size(partialPath) == 0) {
                throw new BusinessException("数据库备份失败: " + dumpResult.output());
            }
            Files.move(partialPath, backupPath);
            ManifestSummary manifest = writeManifest(manifestPath, backupPath);
            verifyArchive(backupPath, target, directory);

            record.setStatus(SUCCESS);
            record.setVerifyStatus(VERIFIED);
            record.setVerifyTime(LocalDateTime.now());
            record.setChecksum(sha256(backupPath));
            record.setFileSize(Files.size(backupPath));
            record.setFileCount(manifest.fileCount());
            record.setMessage("数据库备份、文件清单与归档校验完成");
            finish(record);
            return record.getId();
        } catch (Exception exception) {
            deleteQuietly(partialPath);
            record.setStatus(FAILED);
            record.setVerifyStatus(NOT_VERIFIED);
            record.setMessage(compactMessage(exception));
            finish(record);
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("数据库备份失败: " + compactMessage(exception));
        }
    }

    @Override
    public void verify(Long id) {
        BackupRecord record = require(id);
        Path backupPath = backupPath(record);
        try {
            verifyArchive(backupPath, databaseTarget(), backupDirectory());
            record.setVerifyStatus(VERIFIED);
            record.setVerifyTime(LocalDateTime.now());
            record.setChecksum(sha256(backupPath));
            record.setFileSize(Files.size(backupPath));
            record.setMessage("归档校验通过");
        } catch (Exception exception) {
            record.setVerifyStatus(NOT_VERIFIED);
            record.setMessage("归档校验失败: " + compactMessage(exception));
            if (exception instanceof BusinessException businessException) {
                throw businessException;
            }
            throw new BusinessException("归档校验失败: " + compactMessage(exception));
        } finally {
            record.setUpdateTime(LocalDateTime.now());
            backupRecordMapper.updateById(record);
        }
    }

    @Override
    public void download(Long id, HttpServletResponse response) throws Exception {
        BackupRecord record = require(id);
        Path path = backupPath(record);
        response.setContentType("application/octet-stream");
        response.setContentLengthLong(Files.size(path));
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''"
            + URLEncoder.encode(record.getFileName(), StandardCharsets.UTF_8));
        Files.copy(path, response.getOutputStream());
    }

    @Override
    public void restore(Long id, String confirmation) {
        if (!backupProperties.isRestoreEnabled()) {
            throw new BusinessException("恢复功能默认关闭。请在部署环境显式设置 BACKUP_RESTORE_ENABLED=true 后重启服务");
        }
        if (!("RESTORE-" + id).equals(confirmation)) {
            throw new BusinessException("恢复确认码不正确");
        }
        BackupRecord record = require(id);
        if (record.getStatus() != SUCCESS || record.getVerifyStatus() != VERIFIED) {
            throw new BusinessException("只能恢复已成功且已校验的备份");
        }

        // Always make a fresh rollback point before applying a destructive restore.
        createDatabaseBackup();
        try {
            ProcessResult result = runProcess(restoreCommand(databaseTarget(), backupPath(record)), backupDirectory(), "restore");
            if (result.exitCode() != 0) {
                throw new BusinessException("数据库恢复失败: " + result.output());
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("数据库恢复失败: " + compactMessage(exception));
        }
    }

    private BackupRecord require(Long id) {
        BackupRecord record = backupRecordMapper.selectById(id);
        if (record == null) {
            throw new BusinessException("备份记录不存在");
        }
        return record;
    }

    private void finish(BackupRecord record) {
        record.setEndTime(LocalDateTime.now());
        record.setUpdateTime(record.getEndTime());
        backupRecordMapper.updateById(record);
    }

    private Path backupDirectory() {
        if (!StringUtils.hasText(backupProperties.getDirectory())) {
            throw new BusinessException("未配置 BACKUP_DIRECTORY");
        }
        try {
            Path directory = Paths.get(backupProperties.getDirectory()).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            return directory;
        } catch (IOException exception) {
            throw new BusinessException("备份目录不可用: " + compactMessage(exception));
        }
    }

    private Path backupPath(BackupRecord record) {
        Path directory = backupDirectory();
        Path path = Paths.get(record.getFilePath()).toAbsolutePath().normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) {
            throw new BusinessException("备份文件不存在或路径非法");
        }
        return path;
    }

    private DatabaseTarget databaseTarget() {
        if (!StringUtils.hasText(databaseUrl) || !databaseUrl.startsWith("jdbc:postgresql://")) {
            throw new BusinessException("仅支持 PostgreSQL 数据源备份");
        }
        try {
            URI uri = URI.create(databaseUrl.substring("jdbc:".length()));
            String database = uri.getPath();
            if (!StringUtils.hasText(uri.getHost()) || !StringUtils.hasText(database) || database.length() <= 1) {
                throw new IllegalArgumentException("JDBC URL 缺少主机或数据库名");
            }
            return new DatabaseTarget(uri.getHost(), uri.getPort() < 0 ? 5432 : uri.getPort(), database.substring(1));
        } catch (Exception exception) {
            throw new BusinessException("PostgreSQL JDBC URL 无效");
        }
    }

    private List<String> dumpCommand(DatabaseTarget target, Path targetFile) {
        return List.of(command(backupProperties.getPgDumpCommand(), "pg_dump"), "--format=custom", "--no-owner",
            "--file=" + targetFile, "--host=" + target.host(), "--port=" + target.port(),
            "--username=" + databaseUsername, target.database());
    }

    private List<String> restoreCommand(DatabaseTarget target, Path sourceFile) {
        return List.of(command(backupProperties.getPgRestoreCommand(), "pg_restore"), "--clean", "--if-exists", "--no-owner",
            "--host=" + target.host(), "--port=" + target.port(), "--username=" + databaseUsername,
            "--dbname=" + target.database(), sourceFile.toString());
    }

    private void verifyArchive(Path sourceFile, DatabaseTarget target, Path directory) throws IOException, InterruptedException {
        ProcessResult result = runProcess(List.of(command(backupProperties.getPgRestoreCommand(), "pg_restore"), "--list", sourceFile.toString()),
            directory, "verify");
        if (result.exitCode() != 0 || !StringUtils.hasText(result.output())) {
            throw new BusinessException("备份归档不可恢复: " + result.output());
        }
    }

    private ProcessResult runProcess(List<String> command, Path directory, String operation) throws IOException, InterruptedException {
        Path output = Files.createTempFile(directory, "." + operation + "-", ".log");
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(directory.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(output.toFile());
            if (databasePassword != null) {
                builder.environment().put("PGPASSWORD", databasePassword);
            }
            Process process = builder.start();
            long timeoutMillis = backupProperties.getProcessTimeout().toMillis();
            if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly();
                throw new BusinessException("备份命令执行超时");
            }
            String processOutput = Files.readString(output, StandardCharsets.UTF_8);
            return new ProcessResult(process.exitValue(), compact(processOutput));
        } finally {
            Files.deleteIfExists(output);
        }
    }

    private ManifestSummary writeManifest(Path manifestPath, Path backupPath) throws IOException {
        Path root = Paths.get(fileBasePath).toAbsolutePath().normalize();
        List<Map<String, Object>> files = new ArrayList<>();
        long totalSize = 0;
        if (Files.isDirectory(root)) {
            try (Stream<Path> paths = Files.walk(root)) {
                for (Path path : paths.filter(Files::isRegularFile).toList()) {
                    if (files.size() >= MAX_MANIFEST_FILES) {
                        throw new BusinessException("文件清单超过 " + MAX_MANIFEST_FILES + " 项，拒绝生成不完整清单");
                    }
                    long size = Files.size(path);
                    totalSize += size;
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("path", root.relativize(path).toString().replace('\\', '/'));
                    item.put("size", size);
                    item.put("modifiedTime", Files.getLastModifiedTime(path).toInstant().toString());
                    files.add(item);
                }
            }
        }
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("createdAt", LocalDateTime.now());
        manifest.put("databaseBackup", backupPath.getFileName().toString());
        manifest.put("fileRoot", root.toString());
        manifest.put("fileCount", files.size());
        manifest.put("totalSize", totalSize);
        manifest.put("files", files);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(manifestPath.toFile(), manifest);
        return new ManifestSummary(files.size(), totalSize);
    }

    private String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var inputStream = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private String command(String configuredCommand, String expectedFileName) {
        if (!StringUtils.hasText(configuredCommand)) {
            throw new BusinessException("未配置 " + expectedFileName + " 命令");
        }
        Path commandPath = Paths.get(configuredCommand);
        Path fileName = commandPath.getFileName();
        if (fileName == null || !expectedFileName.equals(fileName.toString())) {
            throw new BusinessException("仅允许执行 " + expectedFileName + " 命令");
        }
        return configuredCommand;
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private String compactMessage(Exception exception) {
        return compact(exception.getMessage());
    }

    private String compact(String value) {
        if (!StringUtils.hasText(value)) {
            return "无详细错误信息";
        }
        String compact = value.replaceAll("[\\r\\n\\t]+", " ").trim();
        return compact.length() > 1000 ? compact.substring(0, 1000) : compact;
    }

    private BackupRecordVO toVO(BackupRecord record) {
        BackupRecordVO result = new BackupRecordVO();
        result.setId(record.getId());
        result.setBackupName(record.getBackupName());
        result.setBackupType(record.getBackupType());
        result.setStatus(record.getStatus());
        result.setVerifyStatus(record.getVerifyStatus());
        result.setFileName(record.getFileName());
        result.setChecksum(record.getChecksum());
        result.setFileSize(record.getFileSize());
        result.setFileCount(record.getFileCount());
        result.setMessage(record.getMessage());
        result.setCreator(record.getCreator());
        result.setStartTime(record.getStartTime());
        result.setEndTime(record.getEndTime());
        result.setVerifyTime(record.getVerifyTime());
        result.setCreateTime(record.getCreateTime());
        return result;
    }

    private record DatabaseTarget(String host, int port, String database) {
    }

    private record ProcessResult(int exitCode, String output) {
    }

    private record ManifestSummary(long fileCount, long totalSize) {
    }
}
