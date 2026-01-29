package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.entity.SysPost;
import com.quinn.hboot.system.model.form.PostForm;
import com.quinn.hboot.system.model.query.PostQuery;
import com.quinn.hboot.system.model.vo.PostVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 岗位信息服务类
 *
 * @author yu-qh
 * @since 2026-01-28 17:27
 */
public interface SysPostService extends IService<SysPost> {

    /**
     *岗位信息分页列表
     *
     * @return {@link IPage<PostVO>} 岗位信息分页列表
     */
    IPage<PostVO> getPostPage(PostQuery queryParams);

    /**
     * 获取岗位信息表单数据
     *
     * @param id 岗位信息ID
     * @return 岗位信息表单数据
     */
    PostForm getPostFormData(Long id);

    /**
     * 新增岗位信息
     *
     * @param formData 岗位信息表单对象
     * @return 是否新增成功
     */
    boolean savePost(PostForm formData);

    /**
     * 修改岗位信息
     *
     * @param id   岗位信息ID
     * @param formData 岗位信息表单对象
     * @return 是否修改成功
     */
    boolean updatePost(Long id, PostForm formData);

    /**
     * 删除岗位信息
     *
     * @param ids 岗位信息ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    boolean deletePosts(String ids);

    Map<Long, String> selectPostNamesByIds(List<Long> longIds);
}
