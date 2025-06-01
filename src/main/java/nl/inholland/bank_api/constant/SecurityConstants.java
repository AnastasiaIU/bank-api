package nl.inholland.bank_api.constant;

public class SecurityConstants {
    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/register",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/h2-console/**"
    };
}
