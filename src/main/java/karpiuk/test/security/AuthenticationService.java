package karpiuk.test.security;

import karpiuk.test.dto.UserLoginRequestDto;
import karpiuk.test.dto.UserLoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public UserLoginResponseDto authenticate(UserLoginRequestDto requestDto) {
        final Authentication authentication = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(requestDto.email(), requestDto.password()));
        String jwtToken = jwtUtil.generateToken(authentication.getName());
        return new UserLoginResponseDto(jwtToken);
    }
}
