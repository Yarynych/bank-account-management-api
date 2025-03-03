package ua.yarynych.apiaccountmanagement.service.auth;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ua.yarynych.apiaccountmanagement.entity.*;
import ua.yarynych.apiaccountmanagement.entity.auth.Token;
import ua.yarynych.apiaccountmanagement.entity.auth.UserAuthDetails;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.AuthResponse;
import ua.yarynych.apiaccountmanagement.entity.exceptions.BadTokenException;

import java.util.Date;
import java.util.HashMap;

@Log4j2
@Service
public class JwtService {
    @Value("${jwt.token.secret}")
    private String jwtSecret;
    @Value("${jwt.token.ttl_ms.access}")
    private long accessValidity;
    @Value("${jwt.token.ttl_ms.refresh}")
    private long refreshValidity;
    @Value("${jwt.token.ttl_ms.reset}")
    private long resetValidity;

    public AuthResponse token(User user) {
        long accessExpiresAt = System.currentTimeMillis() + accessValidity;
        long refreshExpiresAt = System.currentTimeMillis() + refreshValidity;

        String access = generateToken(user, accessExpiresAt, "access");
        String refresh = generateToken(user, refreshExpiresAt, "refresh");

        return new AuthResponse(user.getId(), user.getEmail(), user.getPhone(), user.getFirstName(),
                user.getSecondName(), user.getRole(), user.getCreatedAt(),
                Token.from(access, accessExpiresAt), Token.from(refresh, refreshExpiresAt));
    }

    private String generateToken(User user, long expiresAt, String type) {
        try {
            HashMap<String, Object> claims = new HashMap<>();
            claims.put("type", type);
            claims.put("id", user.getId());
            claims.put("role", user.getRole());

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(user.getEmail())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(expiresAt))
                    .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {
            log.error("Error generating token for user {}: {}", user.getEmail(), e.getMessage());
            throw new BadTokenException("Error generating token");
        }
    }

    public Claims extractClaim(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw new BadTokenException("Error extracting claims");
        }
    }

    public UserAuthDetails read(String token) {
        Claims claims = this.extractClaim(token);
        String email = claims.getSubject();
        Object o = claims.get("role");

        if (o == null) {
            log.warn("Token does not contain 'role' field.");
            throw new BadTokenException("Invalid token data");
        }

        if (o instanceof String) {
            return UserAuthDetails.build(email, (String) o);
        } else {
            log.warn("Unknown class {}", o.getClass().getSimpleName());
            throw new BadTokenException("Invalid token data");
        }
    }

    public String extractType(String token) {
        try {
            return extractClaim(token).get("type", String.class);
        } catch (Exception e) {
            log.error("Error extracting type from token: {}", e.getMessage());
            throw new BadTokenException("Error extracting token type");
        }
    }

    public void validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes())).build().parseClaimsJws(token);
        } catch (ExpiredJwtException expEx) {
            throw new BadTokenException("Token expired");
        } catch (UnsupportedJwtException unsEx) {
            throw new BadTokenException("Unsupported jwt");
        } catch (MalformedJwtException mjEx) {
            log.warn("validateSign: {}", mjEx.getMessage());
            throw new BadTokenException("Malformed jwt");
        } catch (SignatureException sEx) {
            throw new BadTokenException("Invalid signature");
        } catch (Exception e) {
            throw new BadTokenException("Invalid token");
        }
    }
}

