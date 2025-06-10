package org.example.security2.config;

import org.example.security2.RobotFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/sec/public").permitAll();
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(new RobotFilter(), UsernamePasswordAuthenticationFilter.class)
                .formLogin(Customizer.withDefaults());
//        http.sessionManagement(session -> {
//            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
//        });

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder().username("danilo").password("{noop}123").authorities("ROLE_user").build();
        return new InMemoryUserDetailsManager(user);
    }
}
