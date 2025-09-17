package com.vibebooks.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Essencial para APIs REST que não usam sessões
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/usuarios").permitAll() // PERMITE acesso ao endpoint de criação de usuário
                        .anyRequest().authenticated() // EXIGE autenticação para qualquer outra rota
                )
                .build();
    }
}
