package org.example.security2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class RobotFilter extends OncePerRequestFilter {
    private final AuthenticationManager authenticationManager;

    public RobotFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String ROBOT_PASSWORD_FIELD = "x-robot-password";

        if (!Collections.list(request.getHeaderNames()).contains(ROBOT_PASSWORD_FIELD)) {
            filterChain.doFilter(request, response);
            return;
        }

        String password = request.getHeader(ROBOT_PASSWORD_FIELD);
        RobotAuthentication authRequest = RobotAuthentication.unauthenticated(password);

        try {
            Authentication authentication = authenticationManager.authenticate(authRequest);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().println("You are forbidden");
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
