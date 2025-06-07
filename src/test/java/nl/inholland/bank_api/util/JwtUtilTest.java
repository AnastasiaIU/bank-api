package nl.inholland.bank_api.util;

import io.jsonwebtoken.*;
import nl.inholland.bank_api.model.enums.UserRole;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;
import static org.mockito.Mockito.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    @Mock
    private JwtKeyProvider keyProvider;

    @InjectMocks
    private JwtUtil jwtUtil;

    private KeyPair keyPair;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Generate a real RSA key pair once for all tests
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        when(keyProvider.getPrivateKey()).thenReturn(keyPair.getPrivate());
        when(keyProvider.getPublicKey()).thenReturn(keyPair.getPublic());
    }

    // Checks that a token is generated and is not empty.
    @Test
    void generateTokenReturnsValidJwt() {
        String email = "test@example.com";
        UserRole role = UserRole.CUSTOMER;
        Long userId = 123L;

        String token = jwtUtil.generateToken(email, role, userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Parse the token to extract claims
        Jws<Claims> parsedToken = Jwts.parserBuilder()
                .setSigningKey(keyPair.getPublic()) // Use the public key to verify signature
                .build()
                .parseClaimsJws(token);

        Claims claims = parsedToken.getBody();

        assertEquals(email, claims.getSubject());
        assertEquals(role.toString(), claims.get("auth"));

        Number userIdClaim = (Number) claims.get("userId");
        assertEquals(userId.intValue(), userIdClaim.intValue());
    }

    // Tests failure on an invalid token
    @Test
    void validateTokenThrowsJwtExceptionForInvalidToken() {
        // Using an invalid token string to trigger failure
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.invalidsignature";

        assertThrows(JwtException.class, () -> jwtUtil.validateToken(invalidToken));
    }

    static Stream<JwtException> jwtExceptionProvider() {
        return Stream.of(
                new ExpiredJwtException(null, null, "Token expired"),
                new MalformedJwtException("Malformed token"),
                new SignatureException("Invalid token signature"),
                new UnsupportedJwtException("Unsupported token")
        );
    }

    @ParameterizedTest
    @MethodSource("jwtExceptionProvider")
    void sendJwtErrorResponseWritesExceptionDTOForAllExceptions(JwtException exception) throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        jwtUtil.sendJwtErrorResponse(response, exception);

        verify(response).setStatus(401);
        verify(response).setContentType("application/json");

        printWriter.flush();
        String jsonOutput = stringWriter.toString();

        // Check status code is included
        assertTrue(jsonOutput.contains("401"));

        // Check the exception simple class name is included
        assertTrue(jsonOutput.contains(exception.getClass().getSimpleName()));

        // Check the specific message based on exception type
        String expectedMessage = switch (exception.getClass().getSimpleName()) {
            case "ExpiredJwtException" -> "Expired Token";
            case "MalformedJwtException" -> "Malformed token";
            case "SignatureException" -> "Invalid token signature";
            case "UnsupportedJwtException" -> "Unsupported token";
            default -> exception.getMessage();
        };

        assertTrue(jsonOutput.contains(expectedMessage));
    }
}
