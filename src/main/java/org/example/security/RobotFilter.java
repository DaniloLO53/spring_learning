package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class RobotFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String password = request.getHeader("x-robot-password");

        if (!Collections.list(request.getHeaderNames()).contains("x-robot-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("123".equals(password)) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new RobotAuthentication());
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().println("You are not robot!");
        }
    }
}
