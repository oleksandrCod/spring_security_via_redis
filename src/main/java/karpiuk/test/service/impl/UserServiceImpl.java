package karpiuk.test.service.impl;

import java.time.Instant;
import java.util.Set;
import karpiuk.test.dto.LoggedInUserInformationResponseDto;
import karpiuk.test.dto.UserConfirmedRegistrationDto;
import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;
import karpiuk.test.exception.EmailConfirmationTokenException;
import karpiuk.test.exception.RegistrationException;
import karpiuk.test.exception.UserNotFoundException;
import karpiuk.test.mapper.UserMapper;
import karpiuk.test.model.ConfirmationToken;
import karpiuk.test.model.Role;
import karpiuk.test.model.User;
import karpiuk.test.repository.ConfirmationTokenRepository;
import karpiuk.test.repository.RoleRepository;
import karpiuk.test.repository.UserRepository;
import karpiuk.test.service.EmailService;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String CONFIRMATION_TOKEN_ERROR_MESSAGE =
            "Provided confirmation token is invalid or expired!";
    private static final String CONFIRMATION_EMAIL_SUBJECT =
            "Dear customer, please confirm your account registration!";
    private static final String CONFIRMATION_EMAIL_TEXT =
            "To confirm your account, please click here : ";
    private static final String CONFIRMATION_URL =
            "http://localhost:8081/auth/confirm-account?token=";

    private static final String USER_NOT_FOUND_ERROR_MESSAGE =
            "Can't find user by email,"
                    + " User may not register or logged in.";
    private static final String REGISTRATION_ERROR_MESSAGE =
            "Unable to complete registration. Input email already exist: ";
    private static final String CONFIRMATION_USER_NOT_FOUND_ERROR_MESSAGE =
            "User with input username not found!";
    private static final String SUCCESSFUL_CONFIRMATION_MESSAGE =
            "Dear customer your account was activated successfully!";
    private static final String EMAIL_CONFIRMATION_USER_MESSAGE = "Dear customer to proceed"
            + " your registration we sent an email to confirm it!";
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final ConfirmationTokenRepository tokenRepository;
    @Value("${security.admin.email")
    private String adminEmail;
    @Value("${spring.security.email-token.expiration-length}")
    private Long confirmationTokenExpirationLength;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
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

        ConfirmationToken confirmationToken = new ConfirmationToken(user);

        userRepository.save(user);
        tokenRepository.save(confirmationToken);

        SimpleMailMessage confirmationEmail = new SimpleMailMessage();

        confirmationEmail.setTo(user.getEmail());
        confirmationEmail.setSubject(CONFIRMATION_EMAIL_SUBJECT);
        confirmationEmail.setText(CONFIRMATION_EMAIL_TEXT + CONFIRMATION_URL
                + confirmationToken.getConfirmationToken());

        emailService.sendEmail(confirmationEmail);

        return new UserRegistrationResponseDto(EMAIL_CONFIRMATION_USER_MESSAGE);
    }

    @Override
    public LoggedInUserInformationResponseDto getLoggedInUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailIgnoreCase(userEmail)
                .orElseThrow(
                        () -> new UserNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
        return userMapper.toLoggedInResponseDto(user);
    }

    @Override
    public UserConfirmedRegistrationDto confirmEmail(String confirmationToken) {
        ConfirmationToken tokenFromDb = validateToken(confirmationToken);
        User user = fetchUser(tokenFromDb);
        enableUser(user);
        return new UserConfirmedRegistrationDto(SUCCESSFUL_CONFIRMATION_MESSAGE);
    }

    private ConfirmationToken validateToken(String confirmationToken) {
        return tokenRepository.findByConfirmationToken(confirmationToken)
                .filter(token -> token.getCreatedDate()
                        .isBefore(Instant.now().plusMillis(confirmationTokenExpirationLength)))
                .orElseThrow(() -> new EmailConfirmationTokenException(
                        CONFIRMATION_TOKEN_ERROR_MESSAGE));
    }

    private User fetchUser(ConfirmationToken tokenFromDb) {
        return userRepository.findUserByEmailIgnoreCase(tokenFromDb.getUser().getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        CONFIRMATION_USER_NOT_FOUND_ERROR_MESSAGE));
    }

    private void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }
}
