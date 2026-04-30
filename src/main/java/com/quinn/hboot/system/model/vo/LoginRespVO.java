package com.quinn.hboot.system.model.vo;

import com.quinn.hboot.system.model.entity.SysMenu;
import lombok.Data;

import java.util.List;

/**
 * 登录成功响应对象。
 */
@Data
public class LoginRespVO {

    private String token;
    private CurrentUserVO user;
    private List<SysMenu> menus;
}
