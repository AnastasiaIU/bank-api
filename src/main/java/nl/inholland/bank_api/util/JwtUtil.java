package nl.inholland.bank_api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import nl.inholland.bank_api.model.dto.ExceptionDTO;
import nl.inholland.bank_api.model.enums.UserRole;
import nl.inholland.bank_api.service.CustomUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.security.Key;
import java.security.PublicKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    private final JwtKeyProvider keyProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtUtil(JwtKeyProvider keyProvider, CustomUserDetailsService userDetailsService) {
        this.keyProvider = keyProvider;
        this.userDetailsService = userDetailsService;
    }

    public String generateToken(String email, UserRole role, Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000);
        Key privateKey = keyProvider.getPrivateKey();
        return Jwts.builder()
                .setSubject(email)
                .claim("auth", role.name())
                .claim("userId", userId)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(privateKey)
                .compact();
    }

    public Authentication validateToken(String token) {
        PublicKey publicKey = keyProvider.getPublicKey();
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

        } catch (io.jsonwebtoken.JwtException e) {
            // Rethrow to preserve the specific exception type
            throw e;
        }
    }

    public void sendJwtErrorResponse(HttpServletResponse response, JwtException e) throws IOException {
        String message = switch (e.getClass().getSimpleName()) {
            case "ExpiredJwtException" -> "Expired Token";
            case "MalformedJwtException" -> "Malformed token";
            case "SignatureException" -> "Invalid token signature";
            case "UnsupportedJwtException" -> "Unsupported token";
            default -> e.getMessage();
        };

        ExceptionDTO dto = new ExceptionDTO(
                HttpStatus.UNAUTHORIZED.value(),
                e.getClass().getSimpleName(),
                List.of(message));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), dto);
    }

    public <T> T extractClaim(String token, String claimName, Class<T> claimType) {
        PublicKey publicKey = keyProvider.getPublicKey();
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get(claimName, claimType);
    }

    public Long extractUserId(String token) {
        Object userIdClaim = extractClaim(token, "userId", Object.class);

        if (userIdClaim instanceof Number) {
            return ((Number) userIdClaim).longValue();
        }

        return Long.valueOf(userIdClaim.toString());
    }
}