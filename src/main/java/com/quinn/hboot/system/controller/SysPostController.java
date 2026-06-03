package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.system.service.SysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.quinn.hboot.system.model.form.PostForm;
import com.quinn.hboot.system.model.query.PostQuery;
import com.quinn.hboot.system.model.vo.PostVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import cn.dev33.satoken.annotation.SaCheckPermission;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 岗位信息前端控制层
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
@Tag(name = "岗位信息接口")
@RestController
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class SysPostController  {

    private final SysPostService postService;

    @Operation(summary = "岗位信息分页列表")
    @GetMapping("/page")
    @SaCheckPermission("system:post:query")
    @SysLog(module = "岗位信息", type = "分页列表查询")
    public R<?> getPostPage(PostQuery queryParams ) {
        IPage<PostVO> result = postService.getPostPage(queryParams);
        return R.ok(result);
    }

    @Operation(summary = "新增岗位信息")
    @PostMapping
    @SaCheckPermission("system:post:add")
    @SysLog(module = "岗位信息", type = "新增")
    public R<Void> savePost(@RequestBody @Valid PostForm formData ) {
        boolean result = postService.savePost(formData);
        return R.ok();
    }

    @Operation(summary = "获取岗位信息表单数据")
    @GetMapping("/{id}/form")
    @SaCheckPermission("system:post:edit")
    @SysLog(module = "岗位信息", type = "获取表单数据")
    public R<PostForm> getPostForm(
            @Parameter(description = "岗位信息ID") @PathVariable Long id
    ) {
        PostForm formData = postService.getPostFormData(id);
        return R.ok(formData);
    }

    @Operation(summary = "修改岗位信息")
    @PutMapping(value = "/{id}")
    @SaCheckPermission("system:post:edit")
    @SysLog(module = "岗位信息", type = "修改")
    public R<Void> updatePost(
            @Parameter(description = "岗位信息ID") @PathVariable Long id,
            @RequestBody @Validated PostForm formData
    ) {
        boolean result = postService.updatePost(id, formData);
        return R.ok();
    }

    @Operation(summary = "删除岗位信息")
    @DeleteMapping("/{ids}")
    @SaCheckPermission("system:post:delete")
    @SysLog(module = "岗位信息", type = "删除")
    public R<Void> deletePosts(
            @Parameter(description = "岗位信息ID，多个以英文逗号(,)分割") @PathVariable String ids
    ) {
        boolean result = postService.deletePosts(ids);
        return R.ok();
    }
}
