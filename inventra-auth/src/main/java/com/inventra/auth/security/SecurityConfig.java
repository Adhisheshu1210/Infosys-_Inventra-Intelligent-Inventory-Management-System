package com.inventra.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                    // login page
                                "/signup",              // signup page
                                "/forgot",              // forgot password page
                                "/send-otp",            // send OTP endpoint
                                "/verify-otp",          // verify OTP endpoint
                                "/verify-otp-page",     // verify OTP page
                                "/do-login",            // login endpoint
                                "/forgot-password",     // forgot password endpoint
                                "/reset-password",      // reset password page
                                "/reset-password/**",   // reset password endpoint
                                "/css/**",              // CSS files
                                "/js/**",               // JS files
                                "/images/**",           // images
                                "/static/**"            // other static resources
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }
}
