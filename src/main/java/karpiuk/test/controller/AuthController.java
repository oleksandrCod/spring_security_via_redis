package karpiuk.test.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import karpiuk.test.dto.ForgotPasswordRequestDto;
import karpiuk.test.dto.ForgotPasswordResponseDto;
import karpiuk.test.dto.PasswordChangeRequestDto;
import karpiuk.test.dto.ResendEmailConfirmationRequestDto;
import karpiuk.test.dto.ResendEmailConfirmationResponseDto;
import karpiuk.test.dto.ResetPasswordResponseDto;
import karpiuk.test.dto.UserConfirmedRegistrationDto;
import karpiuk.test.dto.UserLoginRequestDto;
import karpiuk.test.dto.UserLoginResponseDto;
import karpiuk.test.dto.UserLogoutResponseDto;
import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;
import karpiuk.test.security.AuthenticationService;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@RequestBody UserLoginRequestDto requestDto) {
        log.info("Received login request for user: {}", requestDto.email());
        return authenticationService.authenticate(requestDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponseDto> register(
            @RequestBody @Valid UserRegistrationRequestDto requestDto) {
        log.info("Received registration request for user: {}", requestDto.getEmail());
        return userService.register(requestDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<UserLogoutResponseDto> logout(HttpServletRequest request) {
        log.info("Received logout request.");
        return authenticationService.logout(request);
    }

    @GetMapping("/confirm-account")
    public ResponseEntity<UserConfirmedRegistrationDto> confirmUserAccount(
            @RequestParam("token") String confirmationToken) {
        log.info("Received confirm account request with token: {}", confirmationToken);
        return userService.confirmEmail(confirmationToken);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponseDto> forgotPassword(
            @RequestBody ForgotPasswordRequestDto requestDto) {
        log.info("Received forgot password request for user: {}", requestDto.email());
        return userService.forgotPasswordValidation(requestDto);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDto> resetPassword(
            @RequestBody PasswordChangeRequestDto requestDto) {
        log.info("Received reset password request");
        return userService.changePassword(requestDto);
    }

    @GetMapping("/resend/email-confirmation")
    public ResponseEntity<ResendEmailConfirmationResponseDto> resendEmail(
            @RequestBody ResendEmailConfirmationRequestDto requestDto) {
        return userService.resendConfirmationEmail(requestDto);
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        log.info("Received ping request!");
        return ResponseEntity.ok("Pong!");
    }
}
