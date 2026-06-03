package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.dto.LoginReqDTO;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.vo.AuthMeVO;
import com.quinn.hboot.system.model.vo.LoginRespVO;
import com.quinn.hboot.system.service.AuthService;
import com.quinn.hboot.system.service.SysUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 认证相关接口。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final SysUserService sysUserService;

    /**
     * 用户登录。
     */
    @PostMapping("/auth/login")
    public R<LoginRespVO> login(@Valid @RequestBody LoginReqDTO req, HttpServletRequest request) {
        return R.ok(authService.login(req, request));
    }

    /**
     * 获取当前登录用户信息。
     */
    @GetMapping("/auth/me")
    public R<AuthMeVO> me() {
        return R.ok(authService.me());
    }

    /**
     * 获取当前登录用户菜单。
     */
    @GetMapping("/auth/menus")
    public R<List<SysMenu>> menus() {
        return R.ok(authService.menus());
    }

    /**
     * 退出登录。
     * <p>
     * 当前前端未强依赖该接口，但后端保留统一退出能力，
     * 便于后续接入单点登录、在线会话管理等功能。
     */
    @PostMapping("/auth/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    /**
     * 注册。
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterUserDTO dto) {
        sysUserService.register(dto);
        return ResponseEntity.ok("注册成功");
    }
}
