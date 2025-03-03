package ua.yarynych.apiaccountmanagement.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.auth.UserAuthDetails;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.AuthResponse;
import ua.yarynych.apiaccountmanagement.entity.enums.Role;
import ua.yarynych.apiaccountmanagement.entity.exceptions.BadTokenException;
import ua.yarynych.apiaccountmanagement.service.auth.JwtService;

import java.util.Date;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;

    @Mock
    private User user;

    private final String secret = "passpasspasspasspasspasspasspasspasspasspasspasspasspasspasspass";
    private final String validToken = Jwts.builder()
            .setSubject("test@example.com")
            .setExpiration(new Date(System.currentTimeMillis() + 10000))
            .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512)
            .compact();

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
        ReflectionTestUtils.setField(jwtService, "accessValidity", 60000L);
        ReflectionTestUtils.setField(jwtService, "refreshValidity", 120000L);
    }

    @Test
    void testTokenGeneration() {
        when(user.getId()).thenReturn(1L);
        when(user.getEmail()).thenReturn("test@example.com");
        when(user.getRole()).thenReturn(Role.ROLE_INTERNAL_USER);

        AuthResponse response = jwtService.token(user);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
    }

    @Test
    void testExtractClaimValidToken() {
        Claims claims = jwtService.extractClaim(validToken);
        assertEquals("test@example.com", claims.getSubject());
    }

    @Test
    void testExtractClaimInvalidToken() {
        String invalidToken = validToken.substring(0, validToken.length() - 5);
        assertThrows(BadTokenException.class, () -> jwtService.extractClaim(invalidToken));
    }

    @Test
    void testReadValidToken() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.ROLE_INTERNAL_USER);

        AuthResponse authResponse = jwtService.token(user);
        String validToken = authResponse.getAccessToken().getToken();

        UserAuthDetails details = jwtService.read(validToken);

        assertEquals("test@example.com", details.getEmail());
    }

    @Test
    void testReadTokenWithoutRoleShouldThrowException() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setRole(null);

        String token = jwtService.token(user).getAccessToken().getToken();

        assertThrows(BadTokenException.class, () -> jwtService.read(token));
    }

    @Test
    void testExtractType() {
        String token = Jwts.builder()
                .setClaims(Map.of("type", "access"))
                .setExpiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512)
                .compact();

        assertEquals("access", jwtService.extractType(token));
    }

    @Test
    void testValidateValidToken() {
        assertDoesNotThrow(() -> jwtService.validateToken(validToken));
    }

    @Test
    void testValidateExpiredToken() {
        String expiredToken = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS512)
                .compact();

        assertThrows(BadTokenException.class, () -> jwtService.validateToken(expiredToken));
    }
}
