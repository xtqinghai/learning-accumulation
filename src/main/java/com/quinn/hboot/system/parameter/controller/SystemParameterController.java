package com.quinn.hboot.system.parameter.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.log.SysLog;
import com.quinn.hboot.system.parameter.model.form.SystemParameterForm;
import com.quinn.hboot.system.parameter.model.query.SystemParameterQuery;
import com.quinn.hboot.system.parameter.model.vo.SystemParameterVO;
import com.quinn.hboot.system.parameter.service.SystemParameterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system-parameter")
@RequiredArgsConstructor
public class SystemParameterController {

    private final SystemParameterService parameterService;

    @GetMapping("/page")
    @SaCheckPermission("system:parameter:query")
    public R<IPage<SystemParameterVO>> page(SystemParameterQuery query) {
        return R.ok(parameterService.page(query));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("system:parameter:query")
    public R<SystemParameterVO> get(@PathVariable Long id) {
        return R.ok(parameterService.get(id));
    }

    @PostMapping
    @SaCheckPermission("system:parameter:add")
    @SysLog(module = "系统参数", type = "新增")
    public R<Long> create(@RequestBody @Valid SystemParameterForm form) {
        return R.ok(parameterService.create(form));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:parameter:edit")
    @SysLog(module = "系统参数", type = "修改")
    public R<Void> update(@PathVariable Long id, @RequestBody @Valid SystemParameterForm form) {
        parameterService.update(id, form);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:parameter:delete")
    @SysLog(module = "系统参数", type = "删除")
    public R<Void> delete(@PathVariable Long id) {
        parameterService.delete(id);
        return R.ok();
    }
}
