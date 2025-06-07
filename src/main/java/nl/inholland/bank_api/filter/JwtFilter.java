package nl.inholland.bank_api.filter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nl.inholland.bank_api.constant.ErrorMessages;
import nl.inholland.bank_api.constant.SecurityConstants;
import nl.inholland.bank_api.util.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {
        try {
            String token = getToken(request);
            String path = request.getRequestURI();

            if (isPublicEndpoint(path)) {
                // Public endpoints continue without token
                chain.doFilter(request, response);
                return;
            }

            if (token == null) {
                // Protected endpoint but token is missing
                throw new JwtException(ErrorMessages.MISSING_TOKEN_OR_AUTHORIZATION_HEADER);
            }

            Authentication authentication = jwtUtil.validateToken(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch (JwtException e) {
            jwtUtil.sendJwtErrorResponse(response, e);
        }
    }

    private String getToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    private boolean isPublicEndpoint(String path) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String pattern : SecurityConstants.PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }

        return false;
    }
}