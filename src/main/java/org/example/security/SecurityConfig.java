//package org.example.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.ProviderManager;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.provisioning.InMemoryUserDetailsManager;
//import org.springframework.security.provisioning.JdbcUserDetailsManager;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import javax.sql.DataSource;
//import java.util.List;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
////        RobotAuthenticationProvider authenticationProvider = new RobotAuthenticationProvider(List.of("1234", "ababab", "123"));
////        ProviderManager providerManager = new ProviderManager(authenticationProvider);
//
//        RobotLoginConfigurer configurer = new RobotLoginConfigurer().password("123").password("abc");
//        http
//                .authorizeHttpRequests(auth -> {
//                    auth.requestMatchers("/public").permitAll();
//                    auth.anyRequest().authenticated();
//                })
//                .with(configurer, Customizer.withDefaults())
////                .addFilterBefore(new RobotFilter(providerManager), UsernamePasswordAuthenticationFilter.class)
////                .authenticationProvider(new DaniloAuthenticationProvider())
//                .formLogin(Customizer.withDefaults());
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
