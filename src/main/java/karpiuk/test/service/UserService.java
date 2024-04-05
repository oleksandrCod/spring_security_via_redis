package karpiuk.test.service;

import java.util.List;
import karpiuk.test.dto.ForgotPasswordRequestDto;
import karpiuk.test.dto.ForgotPasswordResponseDto;
import karpiuk.test.dto.LoggedInUserInformationResponseDto;
import karpiuk.test.dto.PasswordChangeRequestDto;
import karpiuk.test.dto.ResendEmailConfirmationRequestDto;
import karpiuk.test.dto.ResendEmailConfirmationResponseDto;
import karpiuk.test.dto.ResetPasswordResponseDto;
import karpiuk.test.dto.UserConfirmedRegistrationDto;
import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

public interface UserService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto);

    LoggedInUserInformationResponseDto getLoggedInUser();

    UserConfirmedRegistrationDto confirmEmail(String confirmationToke);

    ForgotPasswordResponseDto forgotPasswordValidation(ForgotPasswordRequestDto requestDto);

    ResetPasswordResponseDto changePassword(PasswordChangeRequestDto requestDto);

    List<LoggedInUserInformationResponseDto> getAllUsers(Pageable pageable);

    ResendEmailConfirmationResponseDto resendConfirmationEmail(
            ResendEmailConfirmationRequestDto requestDto);
}
