package com.quinn.hboot.system.service.impl;

import com.quinn.hboot.system.model.entity.SysDept;
import com.quinn.hboot.system.model.entity.SysPost;
import com.quinn.hboot.system.service.SysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quinn.hboot.system.mapper.SysPostMapper;
import com.quinn.hboot.system.model.form.PostForm;
import com.quinn.hboot.system.model.query.PostQuery;
import com.quinn.hboot.system.model.vo.PostVO;
import com.quinn.hboot.system.converter.PostConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

/**
 * 岗位信息服务实现类
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
@Service
@RequiredArgsConstructor
public class SysPostServiceImpl extends ServiceImpl<SysPostMapper, SysPost> implements SysPostService {

    private final PostConverter postConverter;

    /**
     * 获取岗位信息分页列表
     *
     * @param queryParams 查询参数
     * @return {@link IPage<PostVO>} 岗位信息分页列表
     */
    @Override
    public IPage<PostVO> getPostPage(PostQuery queryParams) {
        Page<PostVO> pageVO = this.baseMapper.getPostPage(
                new Page<>(queryParams.getPageNum(), queryParams.getPageSize()),
                queryParams
        );
        return pageVO;
    }

    /**
     * 获取岗位信息表单数据
     *
     * @param id 岗位信息ID
     * @return 岗位信息表单数据
     */
    @Override
    public PostForm getPostFormData(Long id) {
        SysPost entity = this.getById(id);
        return postConverter.toForm(entity);
    }

    /**
     * 新增岗位信息
     *
     * @param formData 岗位信息表单对象
     * @return 是否新增成功
     */
    @Override
    public boolean savePost(PostForm formData) {
        SysPost entity = postConverter.toEntity(formData);
        return this.save(entity);
    }

    /**
     * 更新岗位信息
     *
     * @param id   岗位信息ID
     * @param formData 岗位信息表单对象
     * @return 是否修改成功
     */
    @Override
    public boolean updatePost(Long id,PostForm formData) {
        SysPost entity = postConverter.toEntity(formData);
        entity.setId(id);
        return this.updateById(entity);
    }

    /**
     * 删除岗位信息
     *
     * @param ids 岗位信息ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    @Override
    public boolean deletePosts(String ids) {
        Assert.isTrue(StrUtil.isNotBlank(ids), "删除的岗位信息数据为空");
        // 逻辑删除
        List<Long> idList = Arrays.stream(ids.split(","))
                .map(Long::parseLong)
                .toList();
        return this.removeByIds(idList);
    }

    @Override
    public Map<Long, String> selectPostNamesByIds(List<Long> longIds) {
        List<SysPost> list = this.baseMapper.selectByIds(longIds);
        return list.stream()
                .collect(Collectors.toMap(SysPost::getId, SysPost::getPostName));
    }

}
