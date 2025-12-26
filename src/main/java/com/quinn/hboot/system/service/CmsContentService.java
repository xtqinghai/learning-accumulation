package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.entity.CmsContent;
import com.quinn.hboot.system.model.form.CmsContentForm;
import com.quinn.hboot.system.model.query.CmsContentQuery;
import com.quinn.hboot.system.model.vo.CmsContentVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 系统内容管理服务类
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
public interface CmsContentService extends IService<CmsContent> {

    /**
     *系统内容管理分页列表
     *
     * @return {@link IPage<CmsContentVO>} 系统内容管理分页列表
     */
    IPage<CmsContentVO> getCmsContentPage(CmsContentQuery queryParams);

    /**
     * 获取系统内容管理表单数据
     *
     * @param id 系统内容管理ID
     * @return 系统内容管理表单数据
     */
     CmsContentForm getCmsContentFormData(Long id);

    /**
     * 新增系统内容管理
     *
     * @param formData 系统内容管理表单对象
     * @return 是否新增成功
     */
    boolean saveCmsContent(CmsContentForm formData);

    /**
     * 修改系统内容管理
     *
     * @param id   系统内容管理ID
     * @param formData 系统内容管理表单对象
     * @return 是否修改成功
     */
    boolean updateCmsContent(Long id, CmsContentForm formData);

    /**
     * 删除系统内容管理
     *
     * @param ids 系统内容管理ID，多个以英文逗号(,)分割
     * @return 是否删除成功
     */
    boolean deleteCmsContents(String ids);

}
