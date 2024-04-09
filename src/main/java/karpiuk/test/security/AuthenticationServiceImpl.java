package karpiuk.test.security;

import karpiuk.test.dto.request.UserLoginRequest;
import karpiuk.test.dto.response.UserLoginResponse;
import karpiuk.test.dto.response.UserLogoutResponse;
import karpiuk.test.exception.exceptions.RefreshTokenException;
import karpiuk.test.model.User;
import karpiuk.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final String LOGOUT_MESSAGE = "Logout successful!";
    private static final String INVALID_TOKEN_MESSAGE = "Refresh token is not valid";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Override
    public UserLoginResponse authenticate(UserLoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        log.info("User {} authenticated successfully", request.email());

        return jwtTokenProvider.generateTokens(authentication);
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

        String userEmail = tokenProvider.getUserNameFromJwtToken(refreshToken);

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
