package karpiuk.test.service.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
import karpiuk.test.exception.exceptions.RegistrationException;
import karpiuk.test.exception.exceptions.UserNotFoundException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String CONFIRMATION_EMAIL_SUBJECT =
            "Dear customer, please confirm your account registration!";
    private static final String CONFIRMATION_EMAIL_TEXT =
            "To confirm your account, please click this url: ";
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

    private static final String RESET_PASSWORD_RESPONSE_MESSAGE =
            "Dear customer, to confirm password change we sent you an email!";
    private static final String SUCCESSFUL_PASSWORD_CHANGE = "Your password was change successfully";
    private static final String EMAIL_RESEND_MESSAGE = "Confirmation email was send again!";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final EmailTokenCacheService emailTokenCacheService;
    private final ResetPasswordTokenCacheService resetPasswordTokenCacheService;

    @Value("${spring.security.email-confirmation.url}")
    private String confirmationUrl;

    @Value("${security.admin.email")
    private String adminEmail;

    @Override
    @Transactional
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        validateUniqueEmail(requestDto.getEmail());
        log.info("Registering user with email: {}", requestDto.getEmail());
        User user = createUserFromRequest(requestDto);
        saveUser(user);
        log.info("User registered successfully with email: {}", user.getEmail());
        return sendEmailConfirmation(user);
    }

    private void validateUniqueEmail(String email) throws RegistrationException {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RegistrationException(REGISTRATION_ERROR_MESSAGE + email);
        }
    }

    private User createUserFromRequest(UserRegistrationRequestDto requestDto) {
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        setUserRoles(user);
        return user;
    }

    private void setUserRoles(User user) {
        Set<Role> roles = user.getEmail().equals(adminEmail)
                ? Set.of(roleRepository.getRoleByRoleName(Role.RoleName.ROLE_ADMIN))
                : Set.of(roleRepository.getRoleByRoleName(Role.RoleName.ROLE_USER));
        user.setRoles(roles);
    }

    private void saveUser(User user) {
        userRepository.save(user);
    }

    private UserRegistrationResponseDto sendEmailConfirmation(User user) {
        log.info("Sending email confirmation to user: {}", user.getEmail());
        EmailConfirmationToken emailConfirmationToken = createEmailConfirmationToken(user);
        emailTokenCacheService.addToCache(emailConfirmationToken);
        sendConfirmationEmail(user, emailConfirmationToken.getConfirmationToken());
        return new UserRegistrationResponseDto(EMAIL_CONFIRMATION_USER_MESSAGE);
    }

    private EmailConfirmationToken createEmailConfirmationToken(User user) {
        log.debug("Creating email confirmation token for user: {}", user.getEmail());
        return new EmailConfirmationToken(user);
    }

    private void sendConfirmationEmail(User user, String confirmationToken) {
        SimpleMailMessage confirmationEmail = new SimpleMailMessage();
        confirmationEmail.setTo(user.getEmail());
        confirmationEmail.setSubject(CONFIRMATION_EMAIL_SUBJECT);
        confirmationEmail.setText(CONFIRMATION_EMAIL_TEXT + confirmationUrl + confirmationToken);
        emailService.sendEmail(confirmationEmail);
    }

    @Override
    public ResendEmailConfirmationResponseDto resendConfirmationEmail(
            ResendEmailConfirmationRequestDto requestDto) {
        User user = getUserByEmail(requestDto.email());
        sendEmailConfirmation(user);
        return new ResendEmailConfirmationResponseDto(EMAIL_RESEND_MESSAGE);
    }

    @Override
    public UserConfirmedRegistrationDto confirmEmail(String confirmationToken) {
        EmailConfirmationToken token = validateConfirmationToken(confirmationToken);
        User user = getUserByEmail(token.getUser().getEmail());
        enableUser(user);
        return new UserConfirmedRegistrationDto(SUCCESSFUL_CONFIRMATION_MESSAGE);
    }

    private EmailConfirmationToken validateConfirmationToken(String confirmationToken) {
        return emailTokenCacheService.getEmailToken(confirmationToken);
    }

    private User getUserByEmail(String email) {
        return userRepository.findUserByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
    }

    private void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public ForgotPasswordResponseDto forgotPasswordValidation(
            ForgotPasswordRequestDto requestDto) {
        User user = getUserByEmail(requestDto.email());
        PasswordResetToken resetToken = createPasswordResetToken(user);
        resetPasswordTokenCacheService.addToCache(resetToken);
        sendPasswordResetEmail(user, resetToken.getResetPasswordToken());
        return new ForgotPasswordResponseDto(RESET_PASSWORD_RESPONSE_MESSAGE);
    }

    private PasswordResetToken createPasswordResetToken(User user) {
        return new PasswordResetToken(user);
    }

    private void sendPasswordResetEmail(User user, String resetToken) {
        SimpleMailMessage resetEmail = new SimpleMailMessage();
        resetEmail.setTo(user.getEmail());
        resetEmail.setSubject(RESET_PASSWORD_EMAIL_SUBJECT);
        resetEmail.setText(RESET_PASSWORD_EMAIL_TEXT + resetToken);
        emailService.sendEmail(resetEmail);
    }

    @Override
    public ResetPasswordResponseDto changePassword(PasswordChangeRequestDto requestDto) {
        PasswordResetToken resetToken = validateResetPasswordToken(requestDto.resetToken());
        User user = getUserByEmail(resetToken.getUser().getEmail());
        user.setPassword(passwordEncoder.encode(requestDto.password()));
        userRepository.save(user);
        return new ResetPasswordResponseDto(SUCCESSFUL_PASSWORD_CHANGE);
    }

    private PasswordResetToken validateResetPasswordToken(String resetToken) {
        return resetPasswordTokenCacheService.getPasswordResetToken(resetToken);
    }

    @Override
    public List<LoggedInUserInformationResponseDto> getAllUsers(Pageable pageable) {
        List<User> users = userRepository.findAll(pageable).getContent();
        List<LoggedInUserInformationResponseDto> dtos = users.stream()
                .map(userMapper::toLoggedInResponseDto)
                .toList();
        log.info("Returning {} users.", dtos.size());

    }

    @Override
    public LoggedInUserInformationResponseDto getLoggedInUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = getUserByEmail(userEmail);
        return userMapper.toLoggedInResponseDto(user);
    }
}