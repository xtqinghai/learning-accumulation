package com.quinn.hboot.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quinn.hboot.common.R;
import com.quinn.hboot.common.exception.BusinessException;
import com.quinn.hboot.utils.MapstructUtils;
import com.quinn.hboot.utils.StringUtils;
import com.quinn.hboot.workflow.common.ConditionalOnEnable;
import com.quinn.hboot.workflow.domain.FlwForm;
import com.quinn.hboot.workflow.domain.PageQuery;
import com.quinn.hboot.workflow.domain.bo.FlwFormBo;
import com.quinn.hboot.workflow.domain.vo.FlwFormVo;
import com.quinn.hboot.workflow.mapper.FlwFormMapper;
import com.quinn.hboot.workflow.service.IFlwFormService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.warm.flow.core.service.FormService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 流程表单Service业务层处理
 */
@ConditionalOnEnable
@Slf4j
@RequiredArgsConstructor
@Service
public class FlwFormServiceImpl implements IFlwFormService {

    private final FlwFormMapper baseMapper;

    @Override
    public FlwFormVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    @Override
    public R queryPageList(FlwFormBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<FlwForm> lqw = buildQueryWrapper(bo);
        Page<FlwFormVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return R.ok(result);
    }

    private LambdaQueryWrapper<FlwForm> buildQueryWrapper(FlwFormBo bo) {
        LambdaQueryWrapper<FlwForm> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFormName()), FlwForm::getFormName, bo.getFormName());
        lqw.like(StringUtils.isNotBlank(bo.getFormCode()), FlwForm::getFormCode, bo.getFormCode());
        lqw.eq(Objects.nonNull(bo.getIsPublish()), FlwForm::getIsPublish, bo.getIsPublish());
        lqw.eq(Objects.nonNull(bo.getFormType()), FlwForm::getFormType, bo.getFormType());
        lqw.orderByDesc(FlwForm::getUpdateTime).orderByDesc(FlwForm::getId);
        return lqw;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(FlwFormBo bo) {
        validateContent(bo.getFormType(), bo.getFormContent(), bo.getFormPath());
        validateCode(bo.getFormCode(), bo.getVersion(), null);
        FlwForm add = MapstructUtils.convert(bo, FlwForm.class);
        if (StringUtils.isBlank(add.getVersion())) {
            add.setVersion("1");
        }
        if (Objects.isNull(add.getIsPublish())) {
            add.setIsPublish(0);
        }
        if (Objects.isNull(add.getFormType())) {
            add.setFormType(0);
        }
        if (StringUtils.isBlank(add.getTenantId())) {
            add.setTenantId("000000");
        }
        Date now = new Date();
        add.setCreateTime(now);
        add.setUpdateTime(now);
        add.setDelFlag("0");
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(FlwFormBo bo) {
        if (Objects.isNull(bo.getId())) {
            throw new BusinessException("表单id不能为空");
        }
        FlwForm old = baseMapper.selectById(bo.getId());
        if (Objects.isNull(old)) {
            throw new BusinessException("表单不存在");
        }
        if (old.getIsPublish() != null && old.getIsPublish() == 1) {
            throw new BusinessException("已发布的动态表单不能直接修改，请复制新版本后设计");
        }
        validateContent(bo.getFormType(), bo.getFormContent(), bo.getFormPath());
        validateCode(bo.getFormCode(), bo.getVersion(), bo.getId());
        FlwForm update = MapstructUtils.convert(bo, FlwForm.class);
        update.setCreateTime(old.getCreateTime());
        update.setUpdateTime(new Date());
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        List<FlwForm> forms = baseMapper.selectByIds(ids);
        if (forms.size() != ids.size()) {
            throw new BusinessException("存在已被删除的表单");
        }
        if (forms.stream().anyMatch(item -> item.getIsPublish() != null && item.getIsPublish() == 1)) {
            throw new BusinessException("已发布的动态表单不能删除，请先取消发布");
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publish(Long id) {
        FlwForm form = baseMapper.selectById(id);
        if (Objects.isNull(form)) {
            throw new BusinessException("表单不存在");
        }
        if (form.getFormType() != null && form.getFormType() == 0) {
            validateContent(0, form.getFormContent(), null);
        }
        form.setIsPublish(1);
        form.setUpdateTime(new Date());
        return baseMapper.updateById(form) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unPublish(Long id) {
        FlwForm form = baseMapper.selectById(id);
        if (Objects.isNull(form)) {
            throw new BusinessException("表单不存在");
        }
        long usedCount = baseMapper.countPublishedDefinitionUsage("form:" + form.getFormCode());
        if (usedCount > 0) {
            throw new BusinessException("当前表单已被流程定义引用，不能取消发布");
        }
        form.setIsPublish(0);
        form.setUpdateTime(new Date());
        return baseMapper.updateById(form) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlwFormVo copyForm(Long id) {
        FlwForm old = baseMapper.selectById(id);
        if (Objects.isNull(old)) {
            throw new BusinessException("表单不存在");
        }
        String nextVersion = nextVersion(old.getFormCode());
        FlwForm copy = new FlwForm();
        copy.setFormCode(old.getFormCode());
        copy.setFormName(old.getFormName());
        copy.setVersion(nextVersion);
        copy.setIsPublish(0);
        copy.setFormType(old.getFormType());
        copy.setFormPath(old.getFormPath());
        copy.setFormContent(old.getFormContent());
        copy.setExt(old.getExt());
        copy.setTenantId(old.getTenantId());
        Date now = new Date();
        copy.setCreateTime(now);
        copy.setUpdateTime(now);
        copy.setDelFlag("0");
        baseMapper.insert(copy);
        return MapstructUtils.convert(copy, FlwFormVo.class);
    }

    @Override
    public FlwFormVo getByCode(String formCode) {
        FlwForm form = baseMapper.selectOne(Wrappers.lambdaQuery(FlwForm.class)
            .eq(FlwForm::getFormCode, formCode)
            .eq(FlwForm::getIsPublish, 1)
            .orderByDesc(FlwForm::getId)
            .last("LIMIT 1"));
        return Objects.isNull(form) ? null : baseMapper.selectVoById(form.getId());
    }

    private void validateContent(Integer formType, String formContent, String formPath) {
        Integer type = formType == null ? 0 : formType;
        if (type == 1) {
            if (StringUtils.isBlank(formPath)) {
                throw new BusinessException("外挂表单路径不能为空");
            }
            return;
        }
        if (StringUtils.isBlank(formContent)) {
            throw new BusinessException("表单内容不能为空");
        }
        try {
            JSONObject json = JSONUtil.parseObj(formContent);
            if (json.isEmpty() || !json.containsKey("rule")) {
                throw new BusinessException("表单内容必须包含 rule 配置");
            }
        } catch (JSONException e) {
            throw new BusinessException("表单内容必须是合法 JSON");
        }
    }

    private void validateCode(String formCode, String version, Long id) {
        String ver = StrUtil.blankToDefault(version, "1");
        LambdaQueryWrapper<FlwForm> wrapper = Wrappers.lambdaQuery(FlwForm.class)
            .eq(FlwForm::getFormCode, formCode)
            .eq(FlwForm::getVersion, ver)
            .ne(id != null, FlwForm::getId, id);
        if (baseMapper.exists(wrapper)) {
            throw new BusinessException("当前表单编码和版本已存在");
        }
    }

    private String nextVersion(String formCode) {
        List<FlwForm> forms = baseMapper.selectList(Wrappers.lambdaQuery(FlwForm.class)
            .eq(FlwForm::getFormCode, formCode)
            .orderByDesc(FlwForm::getVersion));
        int max = forms.stream()
            .map(FlwForm::getVersion)
            .filter(StringUtils::isNotBlank)
            .map(item -> {
                try {
                    return Integer.parseInt(item);
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .max(Integer::compareTo)
            .orElse(0);
        return String.valueOf(max + 1);
    }
}
