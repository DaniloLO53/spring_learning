//package org.example.security;
//
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//
//public class RateLimitedAuthenticationProvider implements AuthenticationProvider {
//    private final AuthenticationProvider delegate;
//
//    public RateLimitedAuthenticationProvider(AuthenticationProvider delegate) {
//        this.delegate = delegate;
//    }
//
//    @Override
//    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
//        Authentication parentAuth = delegate.authenticate(authentication);
//        if (isUpdatedCache(parentAuth)) {
//        return parentAuth;
//
//        }
//        throw new BadCredentialsException("Not so fast");
//    }
//
//    private static boolean isUpdatedCache(Authentication parentAuth) {
//        return updatedCache(parentAuth);
//    }
//
//    @Override
//    public boolean supports(Class<?> authentication) {
//        return delegate.supports(authentication);
//    }
//}
