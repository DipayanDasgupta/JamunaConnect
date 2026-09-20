package edu.iitm.jamunaconnect.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Three access levels: public (contact, lookup, map, complaint form),
 * OFFICE_STAFF (triage + transitions), WARDEN (read-only oversight).
 * Everything under /staff requires a login; public pages never do.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/lookup", "/map", "/contacts", "/complaints/new",
                        "/api/rooms", "/api/contacts",
                        "/css/**", "/js/**", "/geo/**", "/images/**", "/favicon.ico",
                        "/actuator/health", "/actuator/health/**", "/actuator/info")
                    .permitAll()
                // Residents file complaints anonymously.
                .requestMatchers(HttpMethod.POST, "/api/complaints").permitAll()
                .requestMatchers("/staff/**").hasAnyRole("OFFICE_STAFF", "WARDEN")
                .requestMatchers("/api/complaints/**", "/api/complaints").hasAnyRole("OFFICE_STAFF", "WARDEN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/staff/login")
                .defaultSuccessUrl("/staff/dashboard", true)
                .permitAll())
            // REST clients (and tests) authenticate with HTTP Basic.
            .httpBasic(basic -> {})
            .logout(logout -> logout
                .logoutUrl("/staff/logout")
                .logoutSuccessUrl("/")
                .permitAll())
            .csrf(csrf -> csrf
                // The complaint form and status PATCH are called by same-origin pages;
                // REST clients authenticate with session cookies.
                .ignoringRequestMatchers("/api/**"));
        return http.build();
    }
}