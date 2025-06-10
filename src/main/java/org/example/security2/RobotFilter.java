package org.example.security2;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RobotFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        System.out.println("***** ROBOT FILTER *****");
        String password = request.getHeader("x-robot-password");
        if ("123".equals(password)) {
            System.out.println("Password OK");

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new RobotAuthentication());
            SecurityContextHolder.setContext(context);

            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().println("You are forbidden");
            System.out.println("Password NOT OK");
        }
    }
}
