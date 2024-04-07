package karpiuk.test.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;

import karpiuk.test.dto.UserLoginResponse;
import karpiuk.test.exception.exceptions.InvalidJwtTokenException;
import karpiuk.test.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service

public class JwtTokenProvider {
    private static final String INVALID_JWT_TOKEN_ERROR_MESSAGE =
            "Provided JWT token is expired or invalid. Please try again!";
    private final StringRedisTemplate redis;

    @Value("${spring.security.jwt.token.expire-length}")
    private Long jwtExpiration;
    @Value("${spring.security.refresh.token.expire-length}")
    private Long refreshTokenExpiration;
    private Key secret;

    public JwtTokenProvider(@Value("${spring.security.jwt.token.secret-key}") String secretKey, StringRedisTemplate redis) {
        secret = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.redis = redis;
    }

    public UserLoginResponse generateTokens(Authentication authentication) {
        User userPrincipal = (User) authentication.getPrincipal();

        Date now = new Date();

        Date expiryDateForAccessToken = new Date(now.getTime() + jwtExpiration);
        String accessToken = Jwts.builder()
                .subject(userPrincipal.getEmail())
                .issuedAt(now)
                .expiration(expiryDateForAccessToken)
                .signWith(secret)
                .compact();

        Date expiryDateForRefreshToken = new Date(now.getTime() + refreshTokenExpiration);
        String refreshToken = Jwts.builder()
                .subject(userPrincipal.getEmail())
                .issuedAt(now)
                .expiration(expiryDateForRefreshToken)
                .signWith(secret)
                .compact();
        return new UserLoginResponse(accessToken, refreshToken);
    }

    public UserLoginResponse refreshTokens(User user, String refreshToken) {
        redis.opsForValue().getAndDelete(refreshToken);
        return generateTokens(new UsernamePasswordAuthenticationToken(user, refreshToken));
    }

    public boolean validateRefreshToken(String refreshToken) {
        if (redis.opsForValue().get(refreshToken) == null) {
            return false;
        }
        return isValidToken(refreshToken);
    }

    public boolean isValidToken(String accessToken) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith((SecretKey) secret)
                    .build()
                    .parseSignedClaims(accessToken);
            return !claimsJws.getPayload().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidJwtTokenException(INVALID_JWT_TOKEN_ERROR_MESSAGE);
        }
    }

    public String getUserNameFromJwtToken(String token) {
        return getClaimsFromToken(token, Claims::getSubject);
    }


    public void invalidateToken(String refreshToken) {
        redis.opsForValue().getAndDelete(refreshToken);
    }

    private <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith((SecretKey) secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
