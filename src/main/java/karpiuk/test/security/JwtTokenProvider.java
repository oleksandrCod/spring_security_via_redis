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
import karpiuk.test.dto.response.UserLoginResponse;
import karpiuk.test.exception.exceptions.InvalidJwtTokenException;
import karpiuk.test.model.User;
import karpiuk.test.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtTokenProvider {
    private static final String INVALID_JWT_TOKEN_ERROR_MESSAGE =
            "Provided JWT token is expired or invalid. Please try again!";
    private final StringRedisTemplate redis;

    @Value("${spring.security.jwt.token.expire-length}")
    private Long jwtExpiration;
    @Value("${spring.security.refresh.token.expire-length}")
    private Long refreshTokenExpiration;
    private final Key jwtSecret;
    private final Key refreshSecret;

    public JwtTokenProvider(@Value("${spring.security.jwt.token.secret-key}") String secretKeyJwt,
                            @Value("${spring.security.refresh.token.secret-key}") String secretKeyRefresh,
                            RedisUtil redisUtil) {
        refreshSecret = Keys.hmacShaKeyFor(secretKeyRefresh.getBytes(StandardCharsets.UTF_8));
        jwtSecret = Keys.hmacShaKeyFor(secretKeyJwt.getBytes(StandardCharsets.UTF_8));

        this.redis = redisUtil.templateForDb(RedisUtil.RedisDb.SECURITY);
    }

    public UserLoginResponse generateTokens(Authentication authentication) {
        log.info("Generating tokens for authentication");

        User userPrincipal = (User) authentication.getPrincipal();

        Date now = new Date();

        Date expiryDateForAccessToken = new Date(now.getTime() + jwtExpiration);
        String accessToken = Jwts.builder()
                .subject(userPrincipal.getEmail())
                .issuedAt(now)
                .expiration(expiryDateForAccessToken)
                .signWith(jwtSecret)
                .compact();

        Date expiryDateForRefreshToken = new Date(now.getTime() + refreshTokenExpiration);
        String refreshToken = Jwts.builder()
                .subject(userPrincipal.getEmail())
                .issuedAt(now)
                .expiration(expiryDateForRefreshToken)
                .signWith(refreshSecret)
                .compact();

        log.info("Tokens generated successfully for user: {}", userPrincipal.getEmail());

        return new UserLoginResponse(accessToken, refreshToken);
    }

    public UserLoginResponse refreshTokens(User user, String refreshToken) {
        log.info("Refreshing tokens for user: {}", user.getEmail());

        redis.opsForValue().getAndDelete(refreshToken);

        log.info("Tokens refreshed successfully for user: {}", user.getEmail());

        return generateTokens(new UsernamePasswordAuthenticationToken(user, refreshToken));
    }

    public boolean validateRefreshToken(String refreshToken) {
        log.info("Validating refresh token");

        if (redis.opsForValue().get(refreshToken) == null) {

            log.info("Refresh token not found in Redis");

            return false;
        }
        boolean isValid = isValidToken(refreshToken);

        log.info("Refresh token validation result: {}", isValid);

        return isValid;
    }

    public boolean isValidToken(String accessToken) {
        log.info("Validating access token");

        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith((SecretKey) jwtSecret)
                    .build()
                    .parseSignedClaims(accessToken);
            boolean isValid = !claimsJws.getPayload().getExpiration().before(new Date());

            log.info("Access token validation result: {}", isValid);

            return isValid;
        } catch (JwtException | IllegalArgumentException exception) {
            log.error("Invalid JWT token: {}", INVALID_JWT_TOKEN_ERROR_MESSAGE);
            throw new InvalidJwtTokenException(INVALID_JWT_TOKEN_ERROR_MESSAGE);
        }
    }

    public String getUserNameFromJwtToken(String token) {
        log.info("Getting username from JWT token");

        String username = getClaimsFromToken(token, Claims::getSubject);

        log.info("Username retrieved from JWT token: {}", username);

        return username;
    }


    public void invalidateToken(String refreshToken) {
        log.info("Invalidating refresh token");

        redis.opsForValue().getAndDelete(refreshToken);

        log.info("Refresh token invalidated successfully");
    }


    private <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver) {
        log.info("getClaimsFromToken method called with token: {}", token);

        final Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith((SecretKey) jwtSecret)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            log.info("Claims parsed successfully");

        } catch (Exception e) {

            log.error("Error parsing claims: {}", e.getMessage());
            throw new InvalidJwtTokenException(INVALID_JWT_TOKEN_ERROR_MESSAGE);
        }
        T result = claimsResolver.apply(claims);
        log.info("Claims resolved successfully");
        return result;
    }
}
