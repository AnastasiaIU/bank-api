package nl.inholland.bank_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(new AntPathRequestMatcher("/**")) // Apply to all endpoints
                .csrf(csrf -> csrf.disable()) // Disable CSRF for testing with Insomnia or frontend
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // To allow H2 frames
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        new AntPathRequestMatcher("/users/register"),
                                        new AntPathRequestMatcher("/users/login"),
                                        new AntPathRequestMatcher("/swagger-ui/**"),
                                        new AntPathRequestMatcher("/v3/api-docs/**"),
                                        new AntPathRequestMatcher("/h2-console/**")
                                ).permitAll() // Public endpoints
                                .anyRequest().authenticated() // All other endpoints require auth
                )
                .httpBasic(Customizer.withDefaults()); // Basic auth fallback for now TODO: replace with JWT

        return http.build();
    }
}
