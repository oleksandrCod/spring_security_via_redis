package karpiuk.test.service;

import karpiuk.test.dto.ForgotPasswordRequestDto;
import karpiuk.test.dto.ForgotPasswordResponseDto;
import karpiuk.test.dto.LoggedInUserInformationResponseDto;
import karpiuk.test.dto.PasswordChangeRequestDto;
import karpiuk.test.dto.ResetPasswordResponseDto;
import karpiuk.test.dto.UserConfirmedRegistrationDto;
import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;
import org.springframework.http.ResponseEntity;

public interface UserService {
    ResponseEntity<UserRegistrationResponseDto> register(UserRegistrationRequestDto requestDto);

    ResponseEntity<LoggedInUserInformationResponseDto> getLoggedInUser();

    ResponseEntity<UserConfirmedRegistrationDto> confirmEmail(String confirmationToke);

    ResponseEntity<ForgotPasswordResponseDto> forgotPasswordValidation(ForgotPasswordRequestDto requestDto);

    ResponseEntity<ResetPasswordResponseDto> changePassword(PasswordChangeRequestDto requestDto);
}
