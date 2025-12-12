package com.quinn.hboot.filter;

import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.system.service.SysUserService;
import com.quinn.hboot.utils.JwtUtils;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yu-qh
 */
public class JwtAuthFilter extends OncePerRequestFilter {
    private final SysUserService userService;
    private final SysMenuService menuService;

    public JwtAuthFilter(SysUserService userService, SysMenuService menuService) {
        this.userService = userService;
        this.menuService = menuService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            String token = h.substring(7);
            try {
                String username = JwtUtils.parse(token);
                if (username != null) {
                    SysUser user = userService.getByUsername(username);
                    if (user != null) {
                        List<String> perms = userService.getUserPermissions(user.getId());
                        List<SimpleGrantedAuthority> authorities = perms.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(user, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (JwtException | IllegalArgumentException ex) {
                // token 无效：可以记录或忽略，让后续拦截返回 401
            }
        }
        chain.doFilter(req, res);
    }
}

