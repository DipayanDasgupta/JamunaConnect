package edu.iitm.jamunaconnect.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.DelegatingAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import edu.iitm.jamunaconnect.service.LoginAttemptService;

import java.util.LinkedHashMap;

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
    public SecurityFilterChain filterChain(HttpSecurity http, LoginAttemptService loginAttempts) throws Exception {
        http
            .addFilterBefore(new LoginRateLimitFilter(loginAttempts), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/lookup", "/map", "/contacts", "/notices", "/emergency", "/wifi",
                        "/complaints/new", "/complaints/track",
                        "/api/rooms", "/api/contacts", "/api/notices", "/api/emergency",
                        "/css/**", "/js/**", "/staff-assets/**", "/images/**", "/favicon.ico",
                        "/actuator/health", "/actuator/health/**", "/actuator/info")
                    .permitAll()
                // Residents file complaints anonymously.
                .requestMatchers(HttpMethod.POST, "/api/complaints").permitAll()
                // Residents check their own complaint with id + tracking token.
                .requestMatchers(HttpMethod.GET, "/api/complaints/*/public").permitAll()
                .requestMatchers("/staff/**").hasAnyRole("OFFICE_STAFF", "WARDEN")
                .requestMatchers("/api/complaints/**", "/api/complaints").hasAnyRole("OFFICE_STAFF", "WARDEN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/staff/login")
                .defaultSuccessUrl("/staff/dashboard", true)
                .permitAll())
            // REST clients (and tests) authenticate with HTTP Basic.
            .httpBasic(basic -> {})
            .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint()))
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

    /**
     * API callers get a 401 + Basic challenge; browser navigation is sent to
     * the staff sign-in page instead of hitting a bare 401.
     */
    @Bean
    public AuthenticationEntryPoint authEntryPoint() {
        BasicAuthenticationEntryPoint basic = new BasicAuthenticationEntryPoint();
        basic.setRealmName("JamunaConnect");
        LinkedHashMap<org.springframework.security.web.util.matcher.RequestMatcher, AuthenticationEntryPoint> map =
                new LinkedHashMap<>();
        map.put(new AntPathRequestMatcher("/api/**"), basic);
        DelegatingAuthenticationEntryPoint entry = new DelegatingAuthenticationEntryPoint(map);
        // Relative so the browser keeps whatever public host it used (Vercel).
        entry.setDefaultEntryPoint((request, response, ex) -> response.sendRedirect("/staff/login"));
        return entry;
    }
}