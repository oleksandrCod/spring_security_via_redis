package karpiuk.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication management",
        description = "Provide endpoints for user login, registration."
                + " Also include endpoints for email confirmation and forgot password flow")
public class AuthController {
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final EmailConfirmationService confirmationService;
    private final ForgotPasswordHandler forgotPasswordHandler;

    @PostMapping("/login")
    @Operation(summary = "Login an existing user",
            description = "Provide login flow, receive request with user credentials. "
                    + "If request successful return response with access token and refresh token. "
                    + "If credential not valid return exception with status BAD_REQUEST")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest requestDto) {

        log.info("Received login request for user: {}", requestDto.email());

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(authenticationService.authenticate(requestDto));
    }

    @PostMapping("/signup")
    @Operation(summary = "Register endpoint",
            description = "Provide registration flow, receive request body with user data. "
                    + "If request is successful return response with status ACCEPTED,"
                    + " also send an email to user for email confirmation.")
    public ResponseEntity<RegistrationResponse> register(
            @RequestBody @Valid UserRegistrationRequest requestDto) {

        log.info("Received registration request for user: {}", requestDto.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(requestDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout endpoint",
            description = "Provide logout flow, exclude user tokens,"
                    + " return response with message and status OK.")
    public ResponseEntity<UserLogoutResponse> logout(@RequestParam("token") String refreshToken) {

        log.info("Received logout refreshToken.");

        return ResponseEntity.ok(authenticationService.logout(refreshToken));
    }

    @GetMapping("/confirm-account")
    @Operation(summary = "Confirm-account endpoint",
            description = "Provide flow for email confirmation, "
                    + "user receive email with special url and secret token, "
                    + "which is used for request validation. "
                    + "If request is valid, user profile change status on confirmed and "
                    + "return message with status OK. "
                    + "If request is not valid, return error message.")
    public ResponseEntity<UserConfirmedRegistrationResponse> confirmUserAccount(
            @RequestParam("token") String confirmationToken) {

        log.info("Received confirm account request with token: {}", confirmationToken);

        return ResponseEntity.ok(confirmationService.confirmEmail(confirmationToken));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot-password endpoint",
            description = "Provide forgot password flow. Receive request user data. "
                    + "If request is valid send an email to user with password reset token, "
                    + "also send response with message and status PROCESSING. "
                    + "If request is not valid return error message with status NOT_FOUND.")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest requestDto) {

        log.info("Received forgot password request for user: {}", requestDto.email());

        return ResponseEntity.status(HttpStatus.PROCESSING)
                .body(forgotPasswordHandler.forgotPasswordValidation(requestDto));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password endpoint",
            description = "Provide password change flow, "
                    + "receive request body with reset token from email and new user password. "
                    + "If request is successful change password to new one, "
                    + "and return message with status ACCEPTED. "
                    + "If request is not valid return error message with status NOT_FOUND.")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @RequestBody PasswordChangeRequest requestDto) {

        log.info("Received reset password request");

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(forgotPasswordHandler.changePassword(requestDto));
    }

    @GetMapping("/resend/email-confirmation")
    @Operation(summary = "Endpoint for resend confirmation email",
            description = "Provide flow for resending email for account confirmation. "
                    + "If request successful, return information message with status ACCEPTED. "
                    + "If request is not valid return error message with status NOT_FOUND.")
    public ResponseEntity<ResendEmailConfirmationResponse> resendEmail(
            @RequestBody ResendEmailConfirmationRequest requestDto) {

        log.info("Received email confirmation resending request");

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(confirmationService.resendConfirmationEmail(requestDto));
    }

    @GetMapping("/current-user")
    @Operation(summary = "User page endpoint",
            description = "Provide current logged-in user information retrieval. "
                    + "If request is successful return logged-in user information with status FOUND. "
                    + "If request is not valid return error message with status NOT_FOUND.")
    public ResponseEntity<LoggedInUserResponse> getLoggedUser() {

        log.info("Received getLoggedUser request");

        return ResponseEntity.status(HttpStatus.FOUND)
                .body(userService.getLoggedInUser());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token endpoint",
            description = "Provide flow for obtaining new refresh token. "
                    + "If request is successful return new access token with refresh token. "
                    + "If request is not valid return error message with status BAD_REQUEST.")
    public ResponseEntity<UserLoginResponse> refresh(@RequestParam("token") String refreshToken) {
        return ResponseEntity.ok().body(authenticationService.refresh(refreshToken));
    }
}
