package org.example.security2.config;

import org.example.security2.DaniloAuthenticationProvider;
import org.example.security2.RobotAuthenticationProvider;
import org.example.security2.RobotFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ProviderManager;
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

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain customSecurityFilterChain(HttpSecurity http) throws Exception {
        ProviderManager authManager = new ProviderManager(new RobotAuthenticationProvider(List.of("1234", "abcd", "123")));

        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/sec/public").permitAll();
                    auth.requestMatchers("/api/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                // comment when working with another project
//                .addFilterBefore(new RobotFilter(authManager), UsernamePasswordAuthenticationFilter.class)
//                .authenticationProvider(new DaniloAuthenticationProvider())
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails userDetails = User.builder().username("user").password("{noop}123").authorities("ROLE_user").build();
        return new InMemoryUserDetailsManager(userDetails);
    }
}

//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests(auth -> {
//                    auth.requestMatchers("/sec/public").permitAll();
//                    auth.anyRequest().authenticated();
//                })
//                .addFilterBefore(new RobotFilter(), UsernamePasswordAuthenticationFilter.class)
//                .formLogin(Customizer.withDefaults());
////        http.sessionManagement(session -> {
////            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
////        });
//
//        return http.build();
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User.builder().username("danilo").password("{noop}123").authorities("ROLE_user").build();
//        return new InMemoryUserDetailsManager(user);
//    }
//}
