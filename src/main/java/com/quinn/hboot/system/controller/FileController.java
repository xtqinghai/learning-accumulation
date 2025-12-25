package com.quinn.hboot.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.system.model.dto.FileChunkUploadDTO;
import com.quinn.hboot.system.model.entity.SysFile;
import com.quinn.hboot.system.service.FileService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("@ss.hasPerm('system:file:query')")
    @SysLog(module = "文件管理", type = "分页列表查询")
    public R<?> getFilePage(FileQuery queryParams ) {
        IPage<FileVO> result = fileService.getFilePage(queryParams);
        return R.ok(result);
    }

    @Operation(summary = "新增文件管理")
    @PostMapping
    @PreAuthorize("@ss.hasPerm('system:file:add')")
    @SysLog(module = "文件管理", type = "新增")
    public R<Void> saveFile(@RequestBody @Valid FileForm formData ) {
        boolean result = fileService.saveFile(formData);
        return R.ok();
    }

    @Operation(summary = "获取文件管理表单数据")
    @GetMapping("/{id}/form")
    @PreAuthorize("@ss.hasPerm('system:file:edit')")
    @SysLog(module = "文件管理", type = "获取表单数据")
    public R<FileForm> getFileForm(
        @Parameter(description = "文件管理ID") @PathVariable Long id
    ) {
        FileForm formData = fileService.getFileFormData(id);
        return R.ok(formData);
    }

    @Operation(summary = "修改文件管理")
    @PutMapping(value = "/{id}")
    @PreAuthorize("@ss.hasPerm('system:file:edit')")
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
    @PreAuthorize("@ss.hasPerm('system:file:delete')")
    @SysLog(module = "文件管理", type = "删除")
    public R<Void> deleteFiles(
        @Parameter(description = "文件管理ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = fileService.deleteFiles(ids);
        return R.ok();
    }


    @PostMapping("/upload")
    public SysFile upload(@RequestParam MultipartFile file) throws Exception {
        return fileService.upload(file);
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) throws Exception {
        fileService.download(id, response);
    }

    @GetMapping("/preview/{id}")
    public void preview(@PathVariable Long id, HttpServletResponse response) throws Exception {
        fileService.preview(id, response);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        fileService.delete(id);
    }

    @DeleteMapping("/batch")
    public void deleteBatch(@RequestBody List<Long> ids) {
        fileService.deleteBatch(ids);
    }

    @GetMapping("/check")
    public R check(@RequestParam String hash) {
        SysFile sysFile = fileService.getOne(
                new LambdaQueryWrapper<SysFile>()
                        .eq(SysFile::getFileHash, hash)
                        .eq(SysFile::getStatus, 1)
                        .last("limit 1")
        );
        return R.ok(sysFile != null);
    }

    @PostMapping("/upload/chunk")
    public void uploadChunk(@RequestPart MultipartFile file,
                            FileChunkUploadDTO dto) throws Exception {
        fileService.uploadChunk(file, dto);
    }

    @PostMapping("/upload/merge")
    public R<SysFile> merge(@RequestBody FileChunkUploadDTO dto) throws Exception {
        return R.ok(fileService.mergeChunks(dto));
    }

}
