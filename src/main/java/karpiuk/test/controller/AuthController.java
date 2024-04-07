package karpiuk.test.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import karpiuk.test.dto.*;
import karpiuk.test.security.AuthenticationService;
import karpiuk.test.service.EmailConfirmationService;
import karpiuk.test.service.PasswordService;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    private final EmailConfirmationService confirmationService;
    private final PasswordService passwordService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest requestDto) {

        log.info("Received login request for user: {}", requestDto.email());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(authenticationService.authenticate(requestDto));
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @RequestBody @Valid UserRegistrationRequest requestDto) {

        log.info("Received registration request for user: {}", requestDto.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(requestDto));
    }

    @PostMapping("/logout")
    public ResponseEntity<UserLogoutResponse> logout(String refreshToken) {

        log.info("Received logout refreshToken.");

        return ResponseEntity.ok(authenticationService.logout(refreshToken));
    }

    @GetMapping("/confirm-account")
    public ResponseEntity<UserConfirmedRegistration> confirmUserAccount(
            @RequestParam("token") String confirmationToken) {

        log.info("Received confirm account request with token: {}", confirmationToken);

        return ResponseEntity.ok(confirmationService.confirmEmail(confirmationToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest requestDto) {

        log.info("Received forgot password request for user: {}", requestDto.email());

        return ResponseEntity.status(HttpStatus.PROCESSING)
                .body(passwordService.forgotPasswordValidation(requestDto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @RequestBody PasswordChangeRequest requestDto) {

        log.info("Received reset password request");

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(passwordService.changePassword(requestDto));
    }

    @GetMapping("/resend/email-confirmation")
    public ResponseEntity<ResendEmailConfirmationResponse> resendEmail(
            @RequestBody ResendEmailConfirmationRequest requestDto) {

        log.info("Received email confirmation resending request");

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(confirmationService.resendConfirmationEmail(requestDto));
    }

    @GetMapping("/current-user")
    public ResponseEntity<LoggedInUserResponse> getLoggedUser() {

        log.info("Received getLoggedUser request");

        return ResponseEntity.status(HttpStatus.FOUND)
                .body(userService.getLoggedInUser());
    }
}
