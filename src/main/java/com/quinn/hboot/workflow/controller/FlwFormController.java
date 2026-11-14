package com.quinn.hboot.workflow.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.workflow.common.ConditionalOnEnable;
import com.quinn.hboot.workflow.domain.PageQuery;
import com.quinn.hboot.workflow.domain.bo.FlwFormBo;
import com.quinn.hboot.workflow.domain.vo.FlwFormVo;
import com.quinn.hboot.workflow.service.IFlwFormService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程动态表单管理
 */
@ConditionalOnEnable
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/workflow/form")
public class FlwFormController {

    private final IFlwFormService flwFormService;

    @GetMapping("/list")
    public R list(FlwFormBo bo, PageQuery pageQuery) {
        return R.ok(flwFormService.queryPageList(bo, pageQuery));
    }

    @GetMapping("/{id}")
    public R<FlwFormVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(flwFormService.queryById(id));
    }

    @GetMapping("/code/{formCode}")
    public R<FlwFormVo> getByCode(@NotEmpty(message = "表单编码不能为空") @PathVariable String formCode) {
        return R.ok(flwFormService.getByCode(formCode));
    }

    @PostMapping
    public R<Void> add(@org.springframework.validation.annotation.Validated @RequestBody FlwFormBo bo) {
        flwFormService.insertByBo(bo);
        return R.ok();
    }

    @PutMapping
    public R<Void> edit(@org.springframework.validation.annotation.Validated @RequestBody FlwFormBo bo) {
        flwFormService.updateByBo(bo);
        return R.ok();
    }

    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        flwFormService.deleteWithValidByIds(List.of(ids));
        return R.ok();
    }

    @PutMapping("/publish/{id}")
    public R<Void> publish(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        flwFormService.publish(id);
        return R.ok();
    }

    @PutMapping("/unPublish/{id}")
    public R<Void> unPublish(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        flwFormService.unPublish(id);
        return R.ok();
    }

    @PostMapping("/copy/{id}")
    public R<FlwFormVo> copy(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(flwFormService.copyForm(id));
    }
}
