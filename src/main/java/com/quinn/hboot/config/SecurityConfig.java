package com.quinn.hboot.config;

import com.quinn.hboot.filter.JwtAuthFilter;
import com.quinn.hboot.system.service.SysMenuService;
import com.quinn.hboot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 *
 * @author yu-qh
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final SysUserService userService;
    private final SysMenuService menuService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtFilter = new JwtAuthFilter(userService, menuService);

        http.csrf(cs -> cs.disable());
        http.cors(cors -> {});
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/public/**","/v3/api-docs/**", "/doc.html","/swagger-resources/**","/webjars/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
