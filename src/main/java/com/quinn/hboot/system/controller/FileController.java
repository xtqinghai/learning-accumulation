package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.system.model.dto.FileChunkUploadDTO;
import com.quinn.hboot.system.model.entity.SysFile;
import com.quinn.hboot.system.service.FileService;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quinn.hboot.system.model.form.FileForm;
import com.quinn.hboot.system.model.query.FileQuery;
import com.quinn.hboot.system.model.vo.FileVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

/**
 * 文件管理前端控制层
 *
 * @author yu-qh
 * @since 2025-12-25 22:55
 */
@Tag(name = "文件管理接口")
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController  {

    private final FileService fileService;

    @Operation(summary = "文件管理分页列表")
    @GetMapping("/page")
    @SaCheckPermission("system:file:query")
    @SysLog(module = "文件管理", type = "分页列表查询")
    public R<?> getFilePage(FileQuery queryParams ) {
        IPage<FileVO> result = fileService.getFilePage(queryParams);
        return R.ok(result);
    }

    
    @Operation(summary = "根据ID批量获取文件")
    @GetMapping("/listByIds/{ids}")
    public R<List<SysFile>> listByIds(
        @Parameter(description = "文件ID，多个用英文逗号分隔") @PathVariable String ids
    ) {
        List<Long> idList = Arrays.stream(ids.split(","))
            .filter(id -> id != null && !id.isBlank())
            .map(Long::parseLong)
            .toList();
        return R.ok(fileService.listByIds(idList));
    }

    @Operation(summary = "新增文件管理")
    @PostMapping
    @SaCheckPermission("system:file:add")
    @SysLog(module = "文件管理", type = "新增")
    public R<Void> saveFile(@RequestBody @Valid FileForm formData ) {
        boolean result = fileService.saveFile(formData);
        return R.ok();
    }

    @Operation(summary = "获取文件管理表单数据")
    @GetMapping("/{id}/form")
    @SaCheckPermission("system:file:edit")
    @SysLog(module = "文件管理", type = "获取表单数据")
    public R<FileForm> getFileForm(
        @Parameter(description = "文件管理ID") @PathVariable Long id
    ) {
        FileForm formData = fileService.getFileFormData(id);
        return R.ok(formData);
    }

    @Operation(summary = "修改文件管理")
    @PutMapping(value = "/{id}")
    @SaCheckPermission("system:file:edit")
    @SysLog(module = "文件管理", type = "修改")
    public R<Void> updateFile(
            @Parameter(description = "文件管理ID") @PathVariable Long id,
            @RequestBody @Validated FileForm formData
    ) {
        boolean result = fileService.updateFile(id, formData);
        return R.ok();
    }

    @Operation(summary = "删除文件管理")
    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:file:delete")
    @SysLog(module = "文件管理", type = "删除")
    public R<Void> deleteFiles(
        @Parameter(description = "文件管理ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = fileService.deleteFiles(ids);
        return R.ok();
    }

    /**
     * 文件上传
     */

    @PostMapping("/upload")
    public R upload(@RequestPart MultipartFile file) throws Exception {
        return R.ok(fileService.upload(file));
    }

    /**
     * 文件下载
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) throws Exception {
        fileService.download(id, response);
    }

    /**
     * 文件预览
     */
    @GetMapping("/preview/{id}")
    public void preview(@PathVariable Long id, HttpServletResponse response) throws Exception {
        fileService.preview(id, response);
    }

    /**
     * 文件校验
     */
    @GetMapping("/check")
    public R check(@RequestParam String hash) {
        SysFile sysFile = fileService.getOne(
                new LambdaQueryWrapper<SysFile>()
                        .eq(SysFile::getOwnerId, SecurityUtils.getUserId())
                        .eq(SysFile::getBizType, "GENERAL")
                        .eq(SysFile::getFileHash, hash)
                        .eq(SysFile::getStatus, 1)
                        .last("limit 1")
        );
        return R.ok(sysFile != null);
    }

    /**
     * 文件分片上传
     */
    @PostMapping("/upload/chunk")
    public void uploadChunk(@RequestPart MultipartFile file,
                            FileChunkUploadDTO dto) throws Exception {
        fileService.uploadChunk(file, dto);
    }

    /**
     * 文件分片合并
     */
    @PostMapping("/upload/merge")
    public R<SysFile> merge(@RequestBody FileChunkUploadDTO dto) throws Exception {
        return R.ok(fileService.mergeChunks(dto));
    }
}
