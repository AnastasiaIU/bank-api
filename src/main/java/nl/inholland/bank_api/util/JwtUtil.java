package nl.inholland.bank_api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import nl.inholland.bank_api.model.dto.ExceptionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.Base64;
import java.util.List;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private Key secretKey;
    private final long expirationMillis = 1000 * 60 * 60; // 1 hour

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public void validateToken(String token) {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
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
                List.of(message)
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), dto);
    }

}
