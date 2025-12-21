package com.quinn.hboot.system.model.entity;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableName;
import com.quinn.hboot.common.base.BaseEntity;

/**
 * 系统操作日志实体对象
 *
 * @author yu-qh
 * @since 2025-12-21 19:25
 */
@Getter
@Setter
@TableName("sys_oper_log")
public class OperLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模块名称
     */
    private String module;
    /**
     * 操作类型
     */
    private String type;
    /**
     * 方法名
     */
    private String method;
    /**
     * 请求方式
     */
    private String requestMethod;
    /**
     * 操作人
     */
    private String operName;
    /**
     * 操作IP
     */
    private String operIp;
    /**
     * 请求URL
     */
    private String operUrl;
    /**
     * 请求参数
     */
    private String operParam;
    /**
     * 返回结果
     */
    private String jsonResult;
    /**
     * 0失败 1成功
     */
    private Integer status;
    /**
     * 错误信息
     */
    private String errorMsg;
    /**
     * 操作时间
     */
    private LocalDateTime operTime;
}
