package karpiuk.test.security;

import karpiuk.test.dto.request.UserLoginRequest;
import karpiuk.test.dto.response.UserLoginResponse;
import karpiuk.test.dto.response.UserLogoutResponse;
import karpiuk.test.exception.exceptions.RefreshTokenException;
import karpiuk.test.exception.exceptions.UserAuthenticationException;
import karpiuk.test.model.User;
import karpiuk.test.repository.UserRepository;
import karpiuk.test.util.HashUtil;
import karpiuk.test.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final String LOGOUT_MESSAGE = "Logout successful!";
    private static final String INVALID_TOKEN_MESSAGE = "Refresh token is not valid";
    public static final String USER_LOGIN_ERROR_MESSAGE = "Unable to login. User credential may not valid.";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final StringRedisTemplate redis;
    private final HashUtil hashUtil;

    public AuthenticationServiceImpl(
            JwtTokenProvider jwtTokenProvider,
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtTokenProvider tokenProvider,
            RedisUtil redisUtil,
            HashUtil hashUtil) {

        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.redis = redisUtil.templateForDb(RedisUtil.RedisDb.SECURITY);
        this.hashUtil = hashUtil;
    }

    @Override
    public UserLoginResponse authenticate(UserLoginRequest request) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()));

            log.info("User {} authenticated successfully", request.email());

            return jwtTokenProvider.generateTokens(authentication);
        } catch (RuntimeException e) {

            log.error("Authentication failed for user {}", request.email());

            throw new UserAuthenticationException(USER_LOGIN_ERROR_MESSAGE);
        }
    }

    @Override
    public UserLogoutResponse logout(String refreshToken) {
        log.info("Attempting to logout user with token: {}", refreshToken);

        validateRefreshToken(refreshToken);
        tokenProvider.invalidateToken(refreshToken);

        SecurityContextHolder.clearContext();

        return new UserLogoutResponse(LOGOUT_MESSAGE);
    }

    @Override
    public UserLoginResponse refresh(String refreshToken) {
        log.info("Attempting to refresh token: {}", refreshToken);

        validateRefreshToken(refreshToken);

        String userEmail = redis.opsForValue().get(hashUtil.hashToSha256(refreshToken));

        User user = userRepository.findByEmailIgnoreCaseAndFetchRoles(userEmail).orElseThrow(
                () -> new RefreshTokenException(INVALID_TOKEN_MESSAGE));

        log.info("Token refreshed successfully for user: {}", userEmail);

        return tokenProvider.refreshTokens(user, refreshToken);
    }

    private void validateRefreshToken(String token) {
        log.info("Validating refresh token: {}", token);

        if (!tokenProvider.validateRefreshToken(token)) {

            log.error("Refresh token validation failed for token: {}", token);

            throw new RefreshTokenException(INVALID_TOKEN_MESSAGE);
        }
        log.info("Refresh token validated successfully: {}", token);
    }
}
