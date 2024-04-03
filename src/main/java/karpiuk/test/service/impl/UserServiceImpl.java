package karpiuk.test.service.impl;

import java.util.Set;
import karpiuk.test.dto.LoggedInUserInformationResponseDto;
import karpiuk.test.dto.UserConfirmedRegistrationDto;
import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;
import karpiuk.test.exception.RegistrationException;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final ConfirmationTokenRepository tokenRepository;
    @Value("${security.admin.email")
    private String adminEmail;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findUserByEmailIgnoreCase(requestDto.getEmail()).isPresent()) {
            throw new RegistrationException(
                    "Unable to complete registration."
                            + "Input email already exist: " + requestDto.getEmail());
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
        confirmationEmail.setSubject("Dear customer, please confirm your account registration!");
        confirmationEmail.setText("To confirm your account, please click here : "
                + "http://localhost:8081/auth/confirm-account?token="
                + confirmationToken.getConfirmationToken());

        emailService.sendEmail(confirmationEmail);

        return new UserRegistrationResponseDto();
    }

    @Override
    public LoggedInUserInformationResponseDto getLoggedInUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findUserByEmailIgnoreCase(userEmail)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Can't find user by email,"
                                + " User may not register or logged in."));
        return userMapper.toLoggedInResponseDto(user);
    }

    @Override
    public UserConfirmedRegistrationDto confirmEmail(String confirmationToken) {
        ConfirmationToken tokenFromDb = tokenRepository
                .findByConfirmationToken(confirmationToken)
                .orElseThrow(() -> new RuntimeException("Confirmation token is invalid."));

        User user = userRepository.findUserByEmailIgnoreCase(tokenFromDb
                        .getUserEntity()
                        .getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User with input username not found!"));
        user.setEnabled(true);
        userRepository.save(user);

        return new UserConfirmedRegistrationDto();
    }
}
