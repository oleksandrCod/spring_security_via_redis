package karpiuk.test.security;

import karpiuk.test.dto.request.UserLoginRequest;
import karpiuk.test.dto.response.UserLoginResponse;
import karpiuk.test.dto.response.UserLogoutResponse;

public interface AuthenticationService {
    UserLoginResponse authenticate(UserLoginRequest request);

    UserLogoutResponse logout(String refreshToken);

    UserLoginResponse refresh(String refreshToken);
}
