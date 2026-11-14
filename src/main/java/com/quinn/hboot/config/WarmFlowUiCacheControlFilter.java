package com.quinn.hboot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * The Warm-Flow UI loads a local compatibility script through a stable file
 * name. Prevent Safari and embedded browsers from reusing an older designer
 * shell after the compatibility layer has changed.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WarmFlowUiCacheControlFilter extends OncePerRequestFilter {

    private static final Set<String> UNCACHED_PATHS = Set.of(
            "/warm-flow-ui/index.html",
            "/warm-flow-ui/compat.js"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (UNCACHED_PATHS.contains(path)) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
        }
        filterChain.doFilter(request, response);
    }
}
