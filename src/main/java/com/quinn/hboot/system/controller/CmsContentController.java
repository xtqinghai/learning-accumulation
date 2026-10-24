package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.system.service.CmsContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quinn.hboot.system.model.form.CmsContentForm;
import com.quinn.hboot.system.model.query.CmsContentQuery;
import com.quinn.hboot.system.model.vo.CmsContentVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * 系统内容管理前端控制层
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
@Tag(name = "系统内容管理接口")
@RestController
@RequestMapping("/api/v1/cms-content")
@RequiredArgsConstructor
public class CmsContentController  {

    private final CmsContentService cmsContentService;

    @Operation(summary = "系统内容管理分页列表")
    @GetMapping("/page")
    @SaCheckPermission("system:cms-content:query")
    @SysLog(module = "系统内容管理", type = "分页列表查询")
    public R<?> getCmsContentPage(CmsContentQuery queryParams ) {
        IPage<CmsContentVO> result = cmsContentService.getCmsContentPage(queryParams);
        return R.ok(result);
    }

    @Operation(summary = "新增系统内容管理")
    @PostMapping
    @SaCheckPermission("system:cms-content:add")
    @SysLog(module = "系统内容管理", type = "新增")
    public R<Void> saveCmsContent(@RequestBody @Valid CmsContentForm formData ) {
        boolean result = cmsContentService.saveCmsContent(formData);
        if (!result) {
            throw new BusinessException("新增内容保存失败");
        }
        return R.ok();
    }

    @Operation(summary = "获取系统内容管理表单数据")
    @GetMapping("/{id}/form")
    @SaCheckPermission("system:cms-content:edit")
    @SysLog(module = "系统内容管理", type = "获取表单数据")
    public R<CmsContentForm> getCmsContentForm(
        @Parameter(description = "系统内容管理ID") @PathVariable Long id
    ) {
        CmsContentForm formData = cmsContentService.getCmsContentFormData(id);
        return R.ok(formData);
    }

    @Operation(summary = "修改系统内容管理")
    @PutMapping(value = "/{id}")
    @SaCheckPermission("system:cms-content:edit")
    @SysLog(module = "系统内容管理", type = "修改")
    public R<Void> updateCmsContent(
            @Parameter(description = "系统内容管理ID") @PathVariable Long id,
            @RequestBody @Validated CmsContentForm formData
    ) {
        boolean result = cmsContentService.updateCmsContent(id, formData);
        if (!result) {
            throw new BusinessException("内容不存在或保存失败");
        }
        return R.ok();
    }

    @Operation(summary = "删除系统内容管理")
    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:cms-content:delete")
    @SysLog(module = "系统内容管理", type = "删除")
    public R<Void> deleteCmsContents(
        @Parameter(description = "系统内容管理ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = cmsContentService.deleteCmsContents(ids);
        return R.ok();
    }
}
