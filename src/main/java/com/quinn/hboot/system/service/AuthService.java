package com.quinn.hboot.system.service;

import com.quinn.hboot.system.model.dto.LoginReqDTO;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.vo.AuthMeVO;
import com.quinn.hboot.system.model.vo.LoginRespVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 认证服务。
 */
public interface AuthService {

    /**
     * 登录。
     */
    LoginRespVO login(LoginReqDTO req, HttpServletRequest request);

    /**
     * 获取当前登录用户信息。
     */
    AuthMeVO me();

    /**
     * 获取当前登录用户菜单。
     */
    List<SysMenu> menus();

    /**
     * 退出登录。
     */
    void logout(String authorizationHeader);
}
