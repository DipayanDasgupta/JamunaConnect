package edu.iitm.jamunaconnect.security;

import edu.iitm.jamunaconnect.service.LoginAttemptService;
import edu.iitm.jamunaconnect.web.ClientIp;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Rejects repeated sign-in attempts from one address or username. */
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final LoginAttemptService attempts;

    public LoginRateLimitFilter(LoginAttemptService attempts) {
        this.attempts = attempts;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())
                && "/staff/login".equals(request.getRequestURI())) {
            String username = request.getParameter("username");
            if (!attempts.allow(ClientIp.resolve(request), username)) {
                response.setStatus(429);
                response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                response.getWriter().write("Too many sign-in attempts. Try again in a minute.");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
