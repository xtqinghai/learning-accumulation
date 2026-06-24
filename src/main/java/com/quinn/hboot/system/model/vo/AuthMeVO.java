package com.quinn.hboot.system.model.vo;

import com.quinn.hboot.system.model.entity.SysMenu;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * 当前登录态信息响应对象。
 */
@Data
public class AuthMeVO {

    private CurrentUserVO user;
    private Set<String> permissions;
    private List<SysMenu> menus;
    private Long activeTimeoutSeconds;
}
