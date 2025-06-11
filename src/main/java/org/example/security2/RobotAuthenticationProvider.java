package org.example.security2;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

public class RobotAuthenticationProvider implements AuthenticationProvider {
    private final List<String> passwords;

    public RobotAuthenticationProvider(List<String> passwords) {
        this.passwords = passwords;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        RobotAuthentication authRequest = (RobotAuthentication) authentication;
        String password = authRequest.getPassword();

        if (passwords.contains(password)) {
            return RobotAuthentication.authenticated();
        } else {
            throw new BadCredentialsException("Bad credentials");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return RobotAuthentication.class.isAssignableFrom(authentication);
    }
}
