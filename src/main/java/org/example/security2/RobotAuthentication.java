//package org.example.security2;
//
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.AuthorityUtils;
//
//import java.util.Collection;
//
//public class RobotAuthentication implements Authentication {
//    @Override
//    public String getName() {
//        return "I'm Danilo";
//    }
//
//    @Override
//    public Object getPrincipal() {
//        return getName();
//    }
//
//    @Override
//    public boolean isAuthenticated() {
//        return true;
//    }
//
//    @Override
//    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
//        System.out.println("Don't do that");
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return AuthorityUtils.createAuthorityList("ROLE_robot");
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
//}
