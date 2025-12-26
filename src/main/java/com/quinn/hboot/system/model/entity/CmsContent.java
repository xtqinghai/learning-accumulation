package com.quinn.hboot.system.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;

/**
 * 系统内容管理实体对象
 *
 * @author yu-qh
 * @since 2025-12-25 15:37
 */
@Getter
@Setter
@TableName("sys_cms_content")
public class CmsContent extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 内容类型
     */
    private String type;
    /**
     * 标题
     */
    private String title;
    /**
     * 摘要
     */
    private String summary;
    /**
     * 正文（HTML）
     */
    private String content;
    /**
     * 0-草稿 1-发布
     */
    private Integer status;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    /**
     * 创建人
     */
    private String creator;
    /**
     * 发布时间
     */
    private LocalDateTime createTime;
}
