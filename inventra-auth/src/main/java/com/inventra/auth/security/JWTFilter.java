package com.inventra.auth.security;

import com.inventra.auth.util.JWTUtility;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JWTFilter implements Filter {

    @Autowired
    private JWTUtility jwtUtility;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtility.extractUsername(token);

            if (email != null) {
                // token is valid → allow
            }
        }

        chain.doFilter(request, response);
    }
}
