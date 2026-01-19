package com.quinn.hboot.system.controller;

import cn.hutool.json.JSONUtil;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.dto.RegisterUserDTO;
import com.quinn.hboot.system.model.entity.SysMenu;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.vo.LoginUser;
import com.quinn.hboot.system.service.LoginLogService;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.system.service.SysUserService;
import com.quinn.hboot.utils.AESUtils;
import com.quinn.hboot.utils.JwtUtils;
import com.quinn.hboot.utils.MenuTreeUtil;
import com.quinn.hboot.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 *
 * @author yu-qh
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService menuService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 登录
     *
     * @param req
     * @return
     */
    @PostMapping("/auth/login")
    public R<LoginResp> login(@RequestBody LoginReq req,
                              HttpServletRequest request) {

        String plainPassword = AESUtils.decrypt(req.getPassword());

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                req.getUsername(),
                                plainPassword
                        )
                );

        LoginUser loginUser = (LoginUser) authentication.getPrincipal();

        // 1️⃣ 生成 tokenId
        String tokenId = UUID.randomUUID().toString();
        String token = JwtUtils.createToken(tokenId);

        // 2️⃣ LoginUser 存 Redis
        redisTemplate.opsForValue().set(
                "login:" + tokenId,
                JSONUtil.toJsonStr(loginUser),
                30,
                TimeUnit.MINUTES
        );

        // 3️⃣ 菜单
        List<SysMenu> menus = menuService.findMenusByUserId(loginUser.getUserId());

        LoginResp resp = new LoginResp();
        resp.setToken(token);
        resp.setUser(loginUser);   // ✔️ 直接返回 LoginUser
        resp.setMenus(MenuTreeUtil.buildTree(menus));

        return R.ok(resp);
    }

    /**
     * 获取当前用户信息
     *
     * @return
     */
    @GetMapping("/auth/me")
    public R<?> me() {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) {
            return R.fail(401, "未登录");
        }
        return R.ok(user);
    }

    /**
     * 获取当前用户菜单
     * @return
     */
    @GetMapping("/auth/menus")
    public R<List<SysMenu>> menus() {
        LoginUser user = SecurityUtils.getLoginUser();
        if (user == null) {
            return R.fail(401, "未登录");
        }

        List<SysMenu> menus = menuService.findMenusByUserId(user.getUserId());
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
    private LoginUser user;
    private List<SysMenu> menus;
}
