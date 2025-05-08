package nl.inholland.bank_api.config;

public class SecurityConstants {
    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/h2-console/**"
    };
}
