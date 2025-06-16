package org.example.security2;

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

public class RobotAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!Collections.list(request.getHeaderNames()).contains("x-robot-secret")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!request.getHeader("x-robot-secret").equals("beep-boop")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().println("Forbidden...");
            return;
        }

        RobotAuthenticationToken auth = new RobotAuthenticationToken();
        SecurityContext newContext = SecurityContextHolder.createEmptyContext();
        newContext.setAuthentication(auth);
        SecurityContextHolder.setContext(newContext);

        filterChain.doFilter(request, response);
    }
}
