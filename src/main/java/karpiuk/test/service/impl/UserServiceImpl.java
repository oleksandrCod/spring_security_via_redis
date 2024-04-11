package karpiuk.test.service.impl;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import karpiuk.test.dto.request.UserRegistrationRequest;
import karpiuk.test.dto.response.LoggedInUserResponse;
import karpiuk.test.dto.response.RegistrationResponse;
import karpiuk.test.exception.handler.exceptions.RegistrationException;
import karpiuk.test.mapper.UserMapper;
import karpiuk.test.model.Role;
import karpiuk.test.model.User;
import karpiuk.test.repository.RoleRepository;
import karpiuk.test.repository.UserRepository;
import karpiuk.test.service.EmailConfirmationService;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String REGISTRATION_ERROR_MESSAGE =
            "Unable to complete registration. Input email already exist: ";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final EmailConfirmationService confirmationService;
    private final ServiceHelper serviceHelper;


    @Value("${security.admin.email")
    private String adminEmail;

    @Override
    @Transactional
    public RegistrationResponse register(UserRegistrationRequest requestDto)
            throws RegistrationException {
        validateUniqueEmail(requestDto.getEmail());

        log.info("Registering user with email: {}", requestDto.getEmail());

        User user = createUserFromRequest(requestDto);
        userRepository.save(user);

        log.info("User registered successfully with email: {}", user.getEmail());

        return confirmationService.sendEmailConfirmation(user);
    }

    @Override
    public List<LoggedInUserResponse> getAllUsers(Pageable pageable) {
        log.info("Fetching all users");

        List<User> users = userRepository.findAll(pageable).getContent();
        List<LoggedInUserResponse> response = users.stream()
                .map(userMapper::toLoggedInResponseDto)
                .toList();

        log.info("Returning {} users.", response.size());

        return response;
    }

    @Override
    public LoggedInUserResponse getLoggedInUser() {
        log.info("Fetching logged in user");

        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = serviceHelper.getUserByEmail(userEmail);

        log.info("Fetched logged in user: {}", userEmail);

        return userMapper.toLoggedInResponseDto(user);
    }

    private void validateUniqueEmail(String email) throws RegistrationException {
        log.info("Validating unique email: {}", email);

        if (userRepository.existsByEmailIgnoreCase(email)) {

            log.error("Registration error: Email already exists: {}", email);

            throw new RegistrationException(REGISTRATION_ERROR_MESSAGE + email);
        }
    }

    private User createUserFromRequest(UserRegistrationRequest requestDto) {
        log.info("Creating user from registration request for email: {}", requestDto.getEmail());

        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(serviceHelper.encodePassword(requestDto.getPassword()));
        user.setFirstName(requestDto.getFirstName());
        user.setLastName(requestDto.getLastName());
        setUserRoles(user);

        log.info("User created from registration request for email: {}", requestDto.getEmail());

        return user;
    }

    private void setUserRoles(User user) {
        log.info("Setting user roles for email: {}", user.getEmail());

        Set<Role> roles = user.getEmail().equals(adminEmail)
                ? Set.of(roleRepository.getRoleByRoleName(Role.RoleName.ROLE_ADMIN))
                : Set.of(roleRepository.getRoleByRoleName(Role.RoleName.ROLE_USER));
        user.setRoles(roles);

        log.info("User roles set for email: {}", user.getEmail());
    }
}