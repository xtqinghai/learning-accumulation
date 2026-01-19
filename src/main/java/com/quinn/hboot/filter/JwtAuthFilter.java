package com.quinn.hboot.filter;

import cn.hutool.json.JSONUtil;
import com.quinn.hboot.common.R;
import com.quinn.hboot.system.model.entity.SysUser;
import com.quinn.hboot.system.model.vo.LoginUser;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.system.service.SysUserService;
import com.quinn.hboot.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yu-qh
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    public JwtAuthFilter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                String tokenId = JwtUtils.parseTokenId(token);

                String key = "login:" + tokenId;
                String json = redisTemplate.opsForValue().get(key);

                if (json != null) {
                    LoginUser loginUser =
                            JSONUtil.toBean(json, LoginUser.class);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    loginUser,
                                    null,
                                    loginUser.getAuthorities()
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);
                }

            } catch (ExpiredJwtException e) {
                write401(res, "登录已过期，请重新登录");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private void write401(HttpServletResponse res, String msg) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(JSONUtil.toJsonStr(R.fail(401, msg)));
    }
}


