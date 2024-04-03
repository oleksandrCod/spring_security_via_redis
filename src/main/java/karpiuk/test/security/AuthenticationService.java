package karpiuk.test.security;

import jakarta.servlet.http.HttpServletRequest;
import karpiuk.test.dto.UserLoginRequestDto;
import karpiuk.test.dto.UserLoginResponseDto;
import karpiuk.test.service.BlackListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final String BEARER_TOKEN = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final int INDEX = 7;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final BlackListingService blackListingService;

    public UserLoginResponseDto authenticate(UserLoginRequestDto requestDto) {
        final Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(requestDto.email(),
                                requestDto.password()));

        String jwtToken = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(jwtToken);
    }

    public void logout(HttpServletRequest request) {
        blackListingService.blackListJwt(getTokenFromHeader(request));
        SecurityContextHolder.clearContext();
    }

    private String getTokenFromHeader(HttpServletRequest req) {
        String bearerToken = req.getHeader(AUTHORIZATION);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TOKEN)) {
            return bearerToken.substring(INDEX);
        }
        return null;
    }
}
