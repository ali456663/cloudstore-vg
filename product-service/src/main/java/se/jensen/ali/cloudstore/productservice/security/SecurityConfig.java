package se.jensen.ali.cloudstore.productservice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final ServiceJwtAuthenticationFilter serviceJwtAuthenticationFilter;

    public SecurityConfig(ServiceJwtAuthenticationFilter serviceJwtAuthenticationFilter) {
        this.serviceJwtAuthenticationFilter = serviceJwtAuthenticationFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/internal/**").permitAll()
                        .requestMatchers("/api/products/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(serviceJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
