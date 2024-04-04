package karpiuk.test.service.impl;

import java.util.Set;
import karpiuk.test.dto.ForgotPasswordRequestDto;
import karpiuk.test.dto.ForgotPasswordResponseDto;
import karpiuk.test.dto.LoggedInUserInformationResponseDto;
import karpiuk.test.dto.PasswordChangeRequestDto;
import karpiuk.test.dto.ResetPasswordResponseDto;
import karpiuk.test.dto.UserConfirmedRegistrationDto;
import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;
import karpiuk.test.exception.EmailConfirmationTokenException;
import karpiuk.test.exception.InvalidPasswordResetToken;
import karpiuk.test.exception.RegistrationException;
import karpiuk.test.exception.UserNotFoundException;
import karpiuk.test.mapper.UserMapper;
import karpiuk.test.model.EmailConfirmationToken;
import karpiuk.test.model.PasswordResetToken;
import karpiuk.test.model.Role;
import karpiuk.test.model.User;
import karpiuk.test.repository.RoleRepository;
import karpiuk.test.repository.UserRepository;
import karpiuk.test.service.EmailService;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String CONFIRMATION_EMAIL_SUBJECT =
            "Dear customer, please confirm your account registration!";
    private static final String CONFIRMATION_EMAIL_TEXT =
            "To confirm your account, please use this identification code: ";
    private static final String EMAIL_CONFIRMATION_USER_MESSAGE = "Dear customer to proceed"
            + " your registration we sent an email to confirm it!";
    private static final String SUCCESSFUL_CONFIRMATION_MESSAGE =
            "Dear customer your account was activated successfully!";
    private static final String RESET_PASSWORD_EMAIL_SUBJECT =
            "Dear customer, an instruction to reset your password write below!";
    private static final String RESET_PASSWORD_EMAIL_TEXT =
            "To confirm your password change, please use this identification code: ";
    private static final String USER_NOT_FOUND_ERROR_MESSAGE =
            "Can't find user by email,"
                    + " User may not register or logged in.";
    private static final String REGISTRATION_ERROR_MESSAGE =
            "Unable to complete registration. Input email already exist: ";
    private static final String CONFIRMATION_USER_NOT_FOUND_ERROR_MESSAGE =
            "User with input username not found!";
    private static final String CONFIRMATION_TOKEN_ERROR_MESSAGE =
            "Provided confirmation token is invalid or expired!";
    private static final String USER_IS_NOT_FOUND_ERROR_MESSAGE = "Can't find user with input email!";
    private static final String RESET_PASSWORD_RESPONSE_MESSAGE =
            "Dear customer, to confirm password change we sent you an email!";
    private static final String SUCCESSFUL_PASSWORD_CHANGE = "Your password was change successfully";
    private static final String RESET_TOKEN_ERROR_MESSAGE =
            "Provided reset token is invalid or expired!";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final EmailTokenCacheService emailTokenCacheService;
    private final ResetPasswordTokenCacheService resetPasswordTokenCacheService;

    @Value("${security.admin.email")
    private String adminEmail;

    @Override
    public ResponseEntity<UserRegistrationResponseDto> register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findUserByEmailIgnoreCase(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException(REGISTRATION_ERROR_MESSAGE + requestDto.getEmail());
        }

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());

        if (user.getEmail().equals(adminEmail)) {
            user.setRoles(Set.of(roleRepository.getRoleByRoleName(Role.RoleName.ROLE_ADMIN)));
        }
        user.setRoles(Set.of(roleRepository.getRoleByRoleName(Role.RoleName.ROLE_USER)));

        EmailConfirmationToken emailConfirmationToken = new EmailConfirmationToken(user);

        userRepository.save(user);
        emailTokenCacheService.addToCache(emailConfirmationToken);

        sendEmailToUser(user, emailConfirmationToken.getConfirmationToken(),
                CONFIRMATION_EMAIL_SUBJECT, CONFIRMATION_EMAIL_TEXT);

        return ResponseEntity.ok(new UserRegistrationResponseDto(EMAIL_CONFIRMATION_USER_MESSAGE));
    }

    @Override
    public ResponseEntity<UserConfirmedRegistrationDto> confirmEmail(String confirmationToken) {
        EmailConfirmationToken tokenFromCache = validateConfirmationToken(confirmationToken);
        User user = fetchUserFromEmailToken(tokenFromCache);
        enableUser(user);

        return ResponseEntity.ok(new UserConfirmedRegistrationDto(SUCCESSFUL_CONFIRMATION_MESSAGE));
    }

    @Override
    public ResponseEntity<ForgotPasswordResponseDto> forgotPasswordValidation(ForgotPasswordRequestDto requestDto) {
        User user = userRepository.findUserByEmailIgnoreCase(requestDto.email())
                .orElseThrow(() -> new UserNotFoundException(USER_IS_NOT_FOUND_ERROR_MESSAGE));

        PasswordResetToken resetToken = new PasswordResetToken(user);
        resetPasswordTokenCacheService.addToCache(resetToken);

        sendEmailToUser(user, resetToken.getResetPasswordToken(),
                RESET_PASSWORD_EMAIL_SUBJECT, RESET_PASSWORD_EMAIL_TEXT);

        return ResponseEntity.ok(new ForgotPasswordResponseDto(RESET_PASSWORD_RESPONSE_MESSAGE));
    }

    @Override
    public ResponseEntity<ResetPasswordResponseDto> changePassword(PasswordChangeRequestDto requestDto) {
        PasswordResetToken resetToken = validateResetPasswordToken(requestDto.resetToken());
        User user = fetchUserFromResetToken(resetToken);
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        userRepository.save(user);
        return ResponseEntity.ok(new ResetPasswordResponseDto(SUCCESSFUL_PASSWORD_CHANGE));
    }

    private EmailConfirmationToken validateConfirmationToken(String confirmationToken) {
        try {
            return emailTokenCacheService.getEmailToken(confirmationToken);
        } catch (EmailConfirmationTokenException e) {
            throw new EmailConfirmationTokenException(CONFIRMATION_TOKEN_ERROR_MESSAGE);
        }
    }

    private User fetchUserFromEmailToken(EmailConfirmationToken token) {
        return userRepository.findUserByEmailIgnoreCase(token.getUser().getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        CONFIRMATION_USER_NOT_FOUND_ERROR_MESSAGE));
    }

    private User fetchUserFromResetToken(PasswordResetToken resetToken) {
        return userRepository.findUserByEmailIgnoreCase(resetToken.getUser().getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        USER_NOT_FOUND_ERROR_MESSAGE));
    }

    private PasswordResetToken validateResetPasswordToken(String passwordResetToken) {
        try {
            return resetPasswordTokenCacheService.getPasswordResetToken(passwordResetToken);
        } catch (InvalidPasswordResetToken e) {
            throw new InvalidPasswordResetToken(RESET_TOKEN_ERROR_MESSAGE);
        }
    }

    private void sendEmailToUser(User user, String token, String subject, String emailText) {
        SimpleMailMessage confirmationEmail = new SimpleMailMessage();

        confirmationEmail.setTo(user.getEmail());
        confirmationEmail.setSubject(subject);
        confirmationEmail.setText(emailText + token);

        emailService.sendEmail(confirmationEmail);
    }

    @Override
    public ResponseEntity<LoggedInUserInformationResponseDto> getLoggedInUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailIgnoreCase(userEmail)
                .orElseThrow(
                        () -> new UserNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
        return ResponseEntity.ok(userMapper.toLoggedInResponseDto(user));
    }

    private void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }
}
