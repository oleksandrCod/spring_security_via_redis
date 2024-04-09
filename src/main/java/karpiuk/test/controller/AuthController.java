package karpiuk.test.controller;

import jakarta.validation.Valid;
import karpiuk.test.dto.request.ForgotPasswordRequest;
import karpiuk.test.dto.request.PasswordChangeRequest;
import karpiuk.test.dto.request.ResendEmailConfirmationRequest;
import karpiuk.test.dto.request.UserLoginRequest;
import karpiuk.test.dto.request.UserRegistrationRequest;
import karpiuk.test.dto.response.ForgotPasswordResponse;
import karpiuk.test.dto.response.LoggedInUserResponse;
import karpiuk.test.dto.response.RegistrationResponse;
import karpiuk.test.dto.response.ResendEmailConfirmationResponse;
import karpiuk.test.dto.response.ResetPasswordResponse;
import karpiuk.test.dto.response.UserConfirmedRegistrationResponse;
import karpiuk.test.dto.response.UserLoginResponse;
import karpiuk.test.dto.response.UserLogoutResponse;
import karpiuk.test.security.AuthenticationService;
import karpiuk.test.service.EmailConfirmationService;
import karpiuk.test.service.ForgotPasswordHandler;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final EmailConfirmationService confirmationService;
    private final ForgotPasswordHandler forgotPasswordHandler;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest requestDto) {

        log.info("Received login request for user: {}", requestDto.email());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(authenticationService.authenticate(requestDto));
    }

    @PostMapping("/signup")
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
    public ResponseEntity<UserConfirmedRegistrationResponse> confirmUserAccount(
            @RequestParam("token") String confirmationToken) {

        log.info("Received confirm account request with token: {}", confirmationToken);

        return ResponseEntity.ok(confirmationService.confirmEmail(confirmationToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest requestDto) {

        log.info("Received forgot password request for user: {}", requestDto.email());

        return ResponseEntity.status(HttpStatus.PROCESSING)
                .body(forgotPasswordHandler.forgotPasswordValidation(requestDto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @RequestBody PasswordChangeRequest requestDto) {

        log.info("Received reset password request");

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(forgotPasswordHandler.changePassword(requestDto));
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

    @PostMapping("/refresh")
    public ResponseEntity<UserLoginResponse> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok().body(authenticationService.refresh(refreshToken));
    }
}
