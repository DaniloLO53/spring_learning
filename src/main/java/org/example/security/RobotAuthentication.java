//package org.example.security;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.AuthorityUtils;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.List;
//
//public class RobotAuthentication implements Authentication {
//
//    private final List<GrantedAuthority> authorities;
//    private final boolean isAuthenticated;
//    private final String password;
//
//    public RobotAuthentication(String password, List<GrantedAuthority> authorities) {
//        this.password = password;
//        this.authorities = authorities;
//        this.isAuthenticated = password == null;
//    }
//
//    public static RobotAuthentication unauthenticated(String password) {
//        return new RobotAuthentication(password, Collections.emptyList());
//    }
//
//    public static RobotAuthentication authenticated() {
//        return new RobotAuthentication(null, AuthorityUtils.createAuthorityList("ROLE_robot"));
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return authorities;
//    }
//
//    @Override
//    public Object getCredentials() {
//        return null;
//    }
//
//    @Override
//    public Object getDetails() {
//        return null;
//    }
//
//    @Override
//    public Object getPrincipal() {
//        return getName();
//    }
//
//    @Override
//    public boolean isAuthenticated() {
//        return isAuthenticated;
//    }
//
//    @Override
//    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
//        throw new IllegalArgumentException("Don't do it");
//    }
//
//    @Override
//    public String getName() {
//        return "Robot";
//    }
//
//    public String getPassword() {
//        return password;
//    }
//}
