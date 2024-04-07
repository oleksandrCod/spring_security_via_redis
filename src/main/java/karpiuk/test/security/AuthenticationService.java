package karpiuk.test.security;

import jakarta.servlet.http.HttpServletRequest;
import karpiuk.test.dto.UserLoginRequest;
import karpiuk.test.dto.UserLoginResponse;
import karpiuk.test.dto.UserLogoutResponse;
import karpiuk.test.exception.exceptions.InvalidJwtTokenException;
import karpiuk.test.exception.exceptions.RefreshTokenException;
import karpiuk.test.model.User;
import karpiuk.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final String LOGOUT_MESSAGE = "Logout successful!";
    private static final String INVALID_TOKEN_MESSAGE = "Refresh token is not valid";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    public UserLoginResponse authenticate(UserLoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        return jwtTokenProvider.generateTokens(authentication);
    }

    public UserLogoutResponse logout(String refreshToken) {
        validateRefreshToken(refreshToken);
        tokenProvider.invalidateToken(refreshToken);

        SecurityContextHolder.clearContext();

        return new UserLogoutResponse(LOGOUT_MESSAGE);
    }

    public UserLoginResponse refresh(String refreshToken) {
        validateRefreshToken(refreshToken);

        String userEmail = tokenProvider.getUserNameFromJwtToken(refreshToken);

        User user = userRepository.findByEmailIgnoreCaseAndFetchRoles(userEmail).orElseThrow(
                () -> new RefreshTokenException(INVALID_TOKEN_MESSAGE));

        return tokenProvider.refreshTokens(user, refreshToken);
    }

    private void validateRefreshToken(String token) {
        if (!tokenProvider.validateRefreshToken(token)) {
            throw new RefreshTokenException(INVALID_TOKEN_MESSAGE);
        }
    }
}
