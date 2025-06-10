//package org.example.security;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
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
//
//import javax.sql.DataSource;
//
//@Configuration
//@EnableWebSecurity
//@EnableMethodSecurity
//public class SecurityConfig {
//	private DataSource dataSource;
//
//	public SecurityConfig(DataSource dataSource) {
//		this.dataSource = dataSource;
//	}
//
//	@Bean
//	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
//		http.authorizeHttpRequests((requests) ->
//				requests
//						.requestMatchers("/h2-console/**").permitAll()
//						.anyRequest().authenticated());
////			http.formLogin(withDefaults());
//		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
//		http.httpBasic(Customizer.withDefaults());
//		http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
//		http.csrf(AbstractHttpConfigurer::disable);
//		return http.build();
//	}
//
//	@Bean
//	public UserDetailsService userDetailsService() {
//		UserDetails user = User.withUsername("user1")
//				.password(passwordEncoder().encode("123"))
//				.roles("USER")
//				.build();
//
//		UserDetails admin = User.withUsername("admin1")
//				.password(passwordEncoder().encode("123"))
//				.roles("ADMIN")
//				.build();
//
//		JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
//		jdbcUserDetailsManager.createUser(user);
//		jdbcUserDetailsManager.createUser(admin);
//
//		return jdbcUserDetailsManager;
////		return new InMemoryUserDetailsManager(user, admin);
//	}
//
//	private PasswordEncoder passwordEncoder() {
//		return new BCryptPasswordEncoder();
//	}
//}
