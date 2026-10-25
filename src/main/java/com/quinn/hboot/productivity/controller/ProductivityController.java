package com.quinn.hboot.productivity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quinn.hboot.common.R;
import com.quinn.hboot.productivity.model.dto.ProductivityRequests;
import com.quinn.hboot.productivity.model.entity.ArticleMeta;
import com.quinn.hboot.productivity.model.entity.DevSnippet;
import com.quinn.hboot.productivity.model.entity.PersonalTask;
import com.quinn.hboot.productivity.model.entity.ProductivityCategory;
import com.quinn.hboot.productivity.model.entity.ProductivityTag;
import com.quinn.hboot.productivity.model.entity.ProjectDocument;
import com.quinn.hboot.productivity.model.entity.TransferRecord;
import com.quinn.hboot.productivity.model.vo.ProductivityOverview;
import com.quinn.hboot.productivity.service.ProductivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Tag(name = "开发效率与知识积累")
@RestController
@RequestMapping("/api/v1/productivity")
@RequiredArgsConstructor
public class ProductivityController {
    private final ProductivityService service;
    private final ObjectMapper objectMapper;

    @Operation(summary = "开发工作台概览")
    @GetMapping("/overview")
    public R<ProductivityOverview> overview() { return R.ok(service.overview()); }

    @GetMapping("/categories")
    public R<List<ProductivityCategory>> categories(@RequestParam(required = false) String type) { return R.ok(service.categories(type)); }

    @PostMapping("/categories")
    public R<ProductivityCategory> saveCategory(@RequestBody @Valid ProductivityRequests.CategorySave request) { return R.ok(service.saveCategory(request)); }

    @DeleteMapping("/categories/{id}")
    public R<Void> deleteCategory(@PathVariable Long id) { service.deleteCategory(id); return R.ok(); }

    @GetMapping("/tags")
    public R<List<ProductivityTag>> tags() { return R.ok(service.tags()); }

    @PostMapping("/tags")
    public R<ProductivityTag> saveTag(@RequestBody @Valid ProductivityRequests.TagSave request) { return R.ok(service.saveTag(request)); }

    @DeleteMapping("/tags/{id}")
    public R<Void> deleteTag(@PathVariable Long id) { service.deleteTag(id); return R.ok(); }

    @GetMapping("/articles/{id}/meta")
    public R<ArticleMeta> articleMeta(@PathVariable Long id) { return R.ok(service.articleMeta(id)); }

    @GetMapping("/articles/{id}/tag-ids")
    public R<List<Long>> articleTags(@PathVariable Long id) { return R.ok(service.articleTagIds(id)); }

    @GetMapping("/articles/{id}/related")
    public R<?> articleRelated(@PathVariable Long id) { return R.ok(service.relatedArticles(id)); }

    @PutMapping("/articles/{id}/meta")
    public R<Void> saveArticleMeta(@PathVariable Long id, @RequestBody @Valid ProductivityRequests.ArticleMetaSave request) { service.saveArticleMeta(id, request); return R.ok(); }

    @GetMapping("/snippets")
    public R<IPage<DevSnippet>> snippets(@RequestParam(required = false) String keyword, @RequestParam(required = false) String language, @RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "20") long size) { return R.ok(service.snippets(keyword, language, page, size)); }

    @PostMapping("/snippets")
    public R<DevSnippet> createSnippet(@RequestBody @Valid ProductivityRequests.SnippetSave request) { return R.ok(service.saveSnippet(null, request)); }

    @PutMapping("/snippets/{id}")
    public R<DevSnippet> updateSnippet(@PathVariable Long id, @RequestBody @Valid ProductivityRequests.SnippetSave request) { return R.ok(service.saveSnippet(id, request)); }

    @DeleteMapping("/snippets/{id}")
    public R<Void> deleteSnippet(@PathVariable Long id) { service.deleteSnippet(id); return R.ok(); }

    @GetMapping("/documents")
    public R<IPage<ProjectDocument>> documents(@RequestParam(required = false) String keyword, @RequestParam(required = false) String type, @RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "20") long size) { return R.ok(service.documents(keyword, type, page, size)); }

    @PostMapping("/documents")
    public R<ProjectDocument> createDocument(@RequestBody @Valid ProductivityRequests.DocumentSave request) { return R.ok(service.saveDocument(null, request)); }

    @PutMapping("/documents/{id}")
    public R<ProjectDocument> updateDocument(@PathVariable Long id, @RequestBody @Valid ProductivityRequests.DocumentSave request) { return R.ok(service.saveDocument(id, request)); }

    @DeleteMapping("/documents/{id}")
    public R<Void> deleteDocument(@PathVariable Long id) { service.deleteDocument(id); return R.ok(); }

    @GetMapping("/tasks")
    public R<IPage<PersonalTask>> tasks(@RequestParam(required = false) String status, @RequestParam(required = false) LocalDate date, @RequestParam(defaultValue = "1") long page, @RequestParam(defaultValue = "50") long size) { return R.ok(service.tasks(status, date, page, size)); }

    @PostMapping("/tasks")
    public R<PersonalTask> createTask(@RequestBody @Valid ProductivityRequests.TaskSave request) { return R.ok(service.saveTask(null, request)); }

    @PutMapping("/tasks/{id}")
    public R<PersonalTask> updateTask(@PathVariable Long id, @RequestBody @Valid ProductivityRequests.TaskSave request) { return R.ok(service.saveTask(id, request)); }

    @PutMapping("/tasks/{id}/status")
    public R<PersonalTask> updateTaskStatus(@PathVariable Long id, @RequestBody @Valid ProductivityRequests.TaskStatusSave request) { return R.ok(service.updateTaskStatus(id, request)); }

    @DeleteMapping("/tasks/{id}")
    public R<Void> deleteTask(@PathVariable Long id) { service.deleteTask(id); return R.ok(); }

    @GetMapping("/transfers")
    public R<List<TransferRecord>> transfers() { return R.ok(service.transfers()); }

    @PostMapping(value = "/transfers/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<TransferRecord> importData(@RequestParam String dataType, @RequestPart("file") MultipartFile file) throws IOException {
        String format = extension(file.getOriginalFilename());
        String payload = ("xlsx".equals(format) || "xls".equals(format) || "excel".equals(format))
            ? "base64:" + Base64.getEncoder().encodeToString(file.getBytes())
            : new String(file.getBytes(), StandardCharsets.UTF_8);
        TransferRecord record = service.createTransfer("IMPORT", dataType.toUpperCase(Locale.ROOT), format, payload);
        return R.ok(processImport(record));
    }

    @PostMapping("/transfers/{id}/retry")
    public R<TransferRecord> retryImport(@PathVariable Long id) { return R.ok(processImport(service.transfer(id))); }

    @GetMapping("/transfers/export")
    public void exportData(@RequestParam String dataType, @RequestParam(defaultValue = "json") String format, HttpServletResponse response) throws IOException {
        String normalizedType = dataType.toUpperCase(Locale.ROOT);
        String normalizedFormat = format.toLowerCase(Locale.ROOT);
        List<?> rows = service.exportRows(normalizedType);
        service.finishTransfer(service.createTransfer("EXPORT", normalizedType, normalizedFormat, null), rows.size(), rows.size(), null);
        String name = "productivity-" + normalizedType.toLowerCase(Locale.ROOT) + "." + normalizedFormat;
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8));
        if ("xlsx".equals(normalizedFormat) || "excel".equals(normalizedFormat)) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            writeWorkbook(rows, response);
        } else {
            response.setContentType("text/" + ("json".equals(normalizedFormat) ? "json" : "plain") + ";charset=UTF-8");
            response.getWriter().write(renderExport(rows, normalizedFormat));
        }
    }

    private TransferRecord processImport(TransferRecord record) {
        int total = 0;
        int success = 0;
        String error = null;
        try {
            List<Map<String, String>> rows = parseRows(record.getPayload(), record.getFormat());
            total = rows.size();
            for (Map<String, String> row : rows) {
                try { importRow(record.getDataType(), row); success++; } catch (Exception ex) { error = ex.getMessage(); }
            }
            if (success != total && error == null) error = "部分记录导入失败";
        } catch (Exception ex) { error = ex.getMessage(); }
        return service.finishTransfer(record, total, success, error);
    }

    private void importRow(String dataType, Map<String, String> row) {
        switch (dataType) {
            case "KNOWLEDGE" -> service.importKnowledge(value(row, "title", "导入知识"), value(row, "content", ""), row.get("keywords"), row.get("summary"), value(row, "status", "DRAFT"));
            case "SNIPPET" -> service.saveSnippet(null, new ProductivityRequests.SnippetSave(value(row, "title", "未命名片段"), value(row, "language", "Other"), longValue(row.get("categoryId")), value(row, "content", ""), row.get("description"), row.get("example"), row.get("tags")));
            case "DOCUMENT" -> service.saveDocument(null, new ProductivityRequests.DocumentSave(value(row, "projectName", "未分类项目"), value(row, "title", "未命名文档"), value(row, "documentType", "TECHNICAL"), longValue(row.get("categoryId")), value(row, "content", ""), row.get("tags"), value(row, "status", "DRAFT")));
            case "TASK" -> service.saveTask(null, new ProductivityRequests.TaskSave(value(row, "title", "未命名任务"), row.get("description"), value(row, "status", "TODO"), value(row, "priority", "MEDIUM"), integerValue(row.get("progress")), dateValue(row.get("planDate")), dateTimeValue(row.get("dueTime")), row.get("recurrenceRule")));
            default -> throw new IllegalArgumentException("导入仅支持 SNIPPET、DOCUMENT、TASK");
        }
    }

    private List<Map<String, String>> parseRows(String payload, String format) throws IOException {
        if ("json".equalsIgnoreCase(format)) {
            List<Map<String, Object>> source = objectMapper.readValue(payload, new TypeReference<>() {});
            return source.stream().map(this::stringifyMap).toList();
        }
        if ("xlsx".equalsIgnoreCase(format) || "xls".equalsIgnoreCase(format) || "excel".equalsIgnoreCase(format)) return parseExcel(payload);
        if ("md".equalsIgnoreCase(format) || "markdown".equalsIgnoreCase(format)) return List.of(Map.of("title", markdownTitle(payload), "content", payload));
        if ("csv".equalsIgnoreCase(format)) return parseCsv(payload);
        throw new IllegalArgumentException("当前仅支持 JSON、CSV、Markdown 和 Excel 导入");
    }

    private List<Map<String, String>> parseExcel(String payload) throws IOException {
        String encoded = payload.startsWith("base64:") ? payload.substring("base64:".length()) : payload;
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)))) {
            var sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) return List.of();
            DataFormatter formatter = new DataFormatter();
            var headerRow = sheet.getRow(sheet.getFirstRowNum());
            List<String> headers = new ArrayList<>();
            for (int column = 0; column < headerRow.getLastCellNum(); column++) headers.add(formatter.formatCellValue(headerRow.getCell(column)));
            List<Map<String, String>> result = new ArrayList<>();
            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                var source = sheet.getRow(rowIndex); if (source == null) continue;
                Map<String, String> row = new LinkedHashMap<>();
                for (int column = 0; column < headers.size(); column++) { Cell cell = source.getCell(column); row.put(headers.get(column), cell == null ? "" : formatter.formatCellValue(cell)); }
                result.add(row);
            }
            return result;
        }
    }

    private List<Map<String, String>> parseCsv(String payload) {
        List<String> lines = payload.lines().filter(line -> !line.isBlank()).toList();
        if (lines.size() < 2) return List.of();
        List<String> headers = splitCsv(lines.get(0));
        List<Map<String, String>> result = new ArrayList<>();
        for (int index = 1; index < lines.size(); index++) {
            List<String> values = splitCsv(lines.get(index)); Map<String, String> row = new LinkedHashMap<>();
            for (int col = 0; col < headers.size(); col++) row.put(headers.get(col), col < values.size() ? values.get(col) : "");
            result.add(row);
        }
        return result;
    }

    private List<String> splitCsv(String line) {
        List<String> result = new ArrayList<>(); StringBuilder current = new StringBuilder(); boolean quoted = false;
        for (int index = 0; index < line.length(); index++) {
            char value = line.charAt(index);
            if (value == '"' && index + 1 < line.length() && line.charAt(index + 1) == '"') { current.append('"'); index++; }
            else if (value == '"') quoted = !quoted;
            else if (value == ',' && !quoted) { result.add(current.toString()); current.setLength(0); }
            else current.append(value);
        }
        result.add(current.toString()); return result;
    }

    private String renderExport(List<?> rows, String format) throws IOException {
        if ("json".equals(format)) return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rows);
        if ("md".equals(format) || "markdown".equals(format)) {
            StringBuilder result = new StringBuilder();
            for (Object row : rows) { Map<String, Object> map = objectMapper.convertValue(row, new TypeReference<>() {}); result.append("# ").append(map.getOrDefault("title", map.getOrDefault("id", "记录"))).append("\n\n").append(map.getOrDefault("content", map.getOrDefault("description", ""))).append("\n\n"); }
            return result.toString();
        }
        List<Map<String, Object>> records = rows.stream().map(row -> objectMapper.convertValue(row, new TypeReference<Map<String, Object>>() {})).toList();
        if (records.isEmpty()) return "";
        List<String> headers = new ArrayList<>(records.get(0).keySet()); StringBuilder result = new StringBuilder(String.join(",", headers)).append('\n');
        for (Map<String, Object> record : records) { for (int index = 0; index < headers.size(); index++) { if (index > 0) result.append(','); result.append(csvValue(String.valueOf(record.getOrDefault(headers.get(index), "")))); } result.append('\n'); }
        return result.toString();
    }

    private void writeWorkbook(List<?> rows, HttpServletResponse response) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("导出数据");
            List<Map<String, Object>> records = rows.stream().map(row -> objectMapper.convertValue(row, new TypeReference<Map<String, Object>>() {})).toList();
            if (!records.isEmpty()) {
                List<String> headers = new ArrayList<>(records.get(0).keySet()); var header = sheet.createRow(0);
                for (int column = 0; column < headers.size(); column++) header.createCell(column).setCellValue(headers.get(column));
                for (int rowIndex = 0; rowIndex < records.size(); rowIndex++) { var row = sheet.createRow(rowIndex + 1); for (int column = 0; column < headers.size(); column++) row.createCell(column).setCellValue(String.valueOf(records.get(rowIndex).getOrDefault(headers.get(column), ""))); }
            }
            workbook.write(response.getOutputStream());
        }
    }

    private Map<String, String> stringifyMap(Map<String, Object> source) { Map<String, String> target = new LinkedHashMap<>(); source.forEach((key, value) -> target.put(key, value == null ? null : String.valueOf(value))); return target; }
    private String extension(String fileName) { if (fileName == null || !fileName.contains(".")) return "txt"; return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT); }
    private String value(Map<String, String> row, String key, String fallback) { String value = row.get(key); return value == null || value.isBlank() ? fallback : value; }
    private Long longValue(String value) { try { return value == null || value.isBlank() ? null : Long.valueOf(value); } catch (NumberFormatException ignored) { return null; } }
    private Integer integerValue(String value) { try { return value == null || value.isBlank() ? null : Integer.valueOf(value); } catch (NumberFormatException ignored) { return null; } }
    private LocalDate dateValue(String value) { try { return value == null || value.isBlank() ? null : LocalDate.parse(value); } catch (Exception ignored) { return null; } }
    private LocalDateTime dateTimeValue(String value) { try { return value == null || value.isBlank() ? null : LocalDateTime.parse(value.replace(" ", "T")); } catch (Exception ignored) { return null; } }
    private String markdownTitle(String markdown) { return markdown.lines().filter(line -> line.startsWith("# ")).map(line -> line.substring(2).trim()).findFirst().orElse("导入 Markdown"); }
    private String csvValue(String value) { return '"' + value.replace("\"", "\"\"") + '"'; }
}
