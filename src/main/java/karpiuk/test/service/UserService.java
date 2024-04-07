package karpiuk.test.service;

import java.util.List;
import karpiuk.test.dto.ForgotPasswordRequest;
import karpiuk.test.dto.ForgotPasswordResponse;
import karpiuk.test.dto.LoggedInUserResponse;
import karpiuk.test.dto.PasswordChangeRequest;
import karpiuk.test.dto.ResendEmailConfirmationRequest;
import karpiuk.test.dto.ResendEmailConfirmationResponse;
import karpiuk.test.dto.ResetPasswordResponse;
import karpiuk.test.dto.UserConfirmedRegistration;
import karpiuk.test.dto.UserRegistrationRequest;
import karpiuk.test.dto.RegistrationResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    RegistrationResponse register(UserRegistrationRequest requestDto);

    LoggedInUserResponse getLoggedInUser();

    List<LoggedInUserResponse> getAllUsers(Pageable pageable);

}
