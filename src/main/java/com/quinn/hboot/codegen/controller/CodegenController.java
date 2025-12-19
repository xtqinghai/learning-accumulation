package com.quinn.hboot.codegen.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.codegen.model.form.GenConfigForm;
import com.quinn.hboot.codegen.model.query.TablePageQuery;
import com.quinn.hboot.codegen.model.vo.CodegenPreviewVO;
import com.quinn.hboot.codegen.model.vo.TablePageVO;
import com.quinn.hboot.codegen.service.CodegenService;
import com.quinn.hboot.codegen.service.GenConfigService;
import com.quinn.hboot.common.R;
import com.quinn.hboot.config.CodegenProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 代码生成器控制层
 *
 * @author Ray
 * @since 2.10.0
 */
@Tag(name = "11.代码生成")
@RestController
@RequestMapping("/api/v1/codegen")
@RequiredArgsConstructor
@Slf4j
public class CodegenController {

    private final CodegenService codegenService;
    private final GenConfigService genConfigService;
    private final CodegenProperties codegenProperties;

    @Operation(summary = "获取数据表分页列表")
    @GetMapping("/table/page")
    public R<?> getTablePage(
            TablePageQuery queryParams
    ) {
        Page<TablePageVO> result = codegenService.getTablePage(queryParams);
        return R.ok(result);
    }

    @Operation(summary = "获取代码生成配置")
    @GetMapping("/{tableName}/config")
    public R<GenConfigForm> getGenConfigFormData(
            @PathVariable String tableName
    ) {
        GenConfigForm formData = genConfigService.getGenConfigFormData(tableName);
        return R.ok(formData);
    }

    @Operation(summary = "保存代码生成配置")
    @PostMapping("/{tableName}/config")
    public R<?> saveGenConfig(@RequestBody GenConfigForm formData) {
        genConfigService.saveGenConfig(formData);
        return R.ok();
    }

    @Operation(summary = "删除代码生成配置")
    @DeleteMapping("/{tableName}/config")
    public R<?> deleteGenConfig(
            @PathVariable String tableName
    ) {
        genConfigService.deleteGenConfig(tableName);
        return R.ok();
    }

    @Operation(summary = "获取预览生成代码")
    @GetMapping("/{tableName}/preview")
    public R<List<CodegenPreviewVO>> getTablePreviewData(@PathVariable String tableName,
                                                              @RequestParam(value = "pageType", required = false, defaultValue = "classic") String pageType) {
        List<CodegenPreviewVO> list = codegenService.getCodegenPreviewData(tableName, pageType);
        return R.ok(list);
    }

    @Operation(summary = "下载代码")
    @GetMapping("/{tableName}/download")
    public void downloadZip(HttpServletResponse response, @PathVariable String tableName,
                            @RequestParam(value = "pageType", required = false, defaultValue = "classic") String pageType) {
        String[] tableNames = tableName.split(",");
        byte[] data = codegenService.downloadCode(tableNames, pageType);

        response.reset();
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(StringUtils.isNotEmpty(tableName)?tableName+".zip":codegenProperties.getDownloadFileName(), StandardCharsets.UTF_8));
        response.setContentType("application/octet-stream; charset=UTF-8");

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            log.error("Error while writing the zip file to response", e);
            throw new RuntimeException("Failed to write the zip file to response", e);
        }
    }
}
