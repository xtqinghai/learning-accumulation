package com.quinn.hboot.system.service.impl;

import com.quinn.hboot.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.CmsContentMapper;
import com.quinn.hboot.system.service.CmsContentService;
import com.quinn.hboot.system.model.entity.CmsContent;
import com.quinn.hboot.system.model.form.CmsContentForm;
import com.quinn.hboot.system.model.query.CmsContentQuery;
import com.quinn.hboot.system.model.vo.CmsContentVO;
import com.quinn.hboot.system.converter.CmsContentConverter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

/**
 * 系统内容管理服务实现类
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
@Service
@RequiredArgsConstructor
public class CmsContentServiceImpl extends ServiceImpl<CmsContentMapper, CmsContent> implements CmsContentService {

    private final CmsContentConverter cmsContentConverter;

    /**
    * 获取系统内容管理分页列表
    *
    * @param queryParams 查询参数
    * @return {@link IPage<CmsContentVO>} 系统内容管理分页列表
    */
    @Override
    public IPage<CmsContentVO> getCmsContentPage(CmsContentQuery queryParams) {
        Page<CmsContentVO> pageVO = this.baseMapper.getCmsContentPage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams
        );
        return pageVO;
    }

    /**
     * 获取系统内容管理表单数据
     *
     * @param id 系统内容管理ID
     * @return 系统内容管理表单数据
     */
    @Override
    public CmsContentForm getCmsContentFormData(Long id) {
        CmsContent entity = this.getById(id);
        return cmsContentConverter.toForm(entity);
    }

    /**
     * 新增系统内容管理
     *
     * @param formData 系统内容管理表单对象
     * @return 是否新增成功
     */
    @Override
    public boolean saveCmsContent(CmsContentForm formData) {
        CmsContent entity = cmsContentConverter.toEntity(formData);
        entity.setCreator(SecurityUtils.getUsername());
        entity.setCreateTime(LocalDateTime.now());
        if(entity.getPublishTime()==null && entity.getStatus()==1){
            entity.setPublishTime(LocalDateTime.now());
        }
        return this.save(entity);
    }

    /**
     * 更新系统内容管理
     *
     * @param id   系统内容管理ID
     * @param formData 系统内容管理表单对象
     * @return 是否修改成功
     */
    @Override
    public boolean updateCmsContent(Long id,CmsContentForm formData) {
        CmsContent entity = cmsContentConverter.toEntity(formData);
        entity.setId(id);
        return this.updateById(entity);
    }

    /**
     * 删除系统内容管理
     *
     * @param ids 系统内容管理ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    @Override
    public boolean deleteCmsContents(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的系统内容管理数据为空");
        // 逻辑删除
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        return this.removeByIds(idList);
    }

}
