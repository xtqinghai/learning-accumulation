package com.quinn.hboot.system.controller;

import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.service.LoginLogService;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.system.service.SysUserService;
import com.quinn.hboot.utils.AESUtils;
import com.quinn.hboot.utils.JwtUtils;
import com.quinn.hboot.utils.MenuTreeUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author yu-qh
 */
@RestController
@RequestMapping("/api")
public class AuthController {

//    @Autowired
//    private AuthService userService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService menuService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private LoginLogService loginLogService;

    /**
     * 登录
     *
     * @param req
     * @return
     */
    @PostMapping("/auth/login")
    public R<LoginResp> login(@RequestBody LoginReq req, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        SysUser user =  sysUserService.getByUsername(req.getUsername());
        if (user == null) {
            loginLogService.record(ip,req.getUsername(), false, "用户不存在");
            return R.fail(401,"用户不存在");
        }

        // 1. 解密前端传递的加密密码
        String plainPassword = AESUtils.decrypt(req.getPassword());

        if (!passwordEncoder.matches(plainPassword, user.getPassword())) {
            loginLogService.record(ip,req.getUsername(), false, "用户名或密码错误");
            return R.fail(401,"用户名或密码错误");
        }

        String token = JwtUtils.createToken(user.getUsername());
        List<SysMenu> menus = menuService.findMenusByUserId(user.getId());
        List<SysMenu> menuTree = MenuTreeUtil.buildTree(menus);

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUser(user);
        resp.setMenus(menuTree);

        loginLogService.record(ip,req.getUsername(), true, "登陆成功");
        return R.ok(resp);
    }

    /**
     * 获取当前用户信息
     *
     * @param authentication
     * @return
     */
    @GetMapping("/auth/me")
    public R<?> me(Authentication authentication) {
        if (authentication == null) {
            return R.fail(401,"未登录");
        }
        SysUser user = (SysUser) authentication.getPrincipal();
        List<String> perms = sysUserService.getUserPermissions(user.getId());
        return R.ok(Map.of("user", user, "permissions", perms));
    }

    /**
     * 获取当前用户菜单
     *
     * @param authentication
     * @return
     */
    @GetMapping("/auth/menus")
    public R<List<SysMenu>> menus(Authentication authentication) {
        SysUser user = (SysUser) authentication.getPrincipal();
        List<SysMenu> menus = menuService.findMenusByUserId(user.getId());
        return R.ok(MenuTreeUtil.buildTree(menus));
    }

    /**
     * 注册
     *
     * @param dto
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Validated @RequestBody RegisterUserDTO dto) {
        sysUserService.register(dto);
        return ResponseEntity.ok("注册成功");
    }
}

@Data
class LoginReq {
    private String username;
    private String password;
}

@Data
class LoginResp {
    private String token;
    private SysUser user;
    private List<SysMenu> menus;
}
