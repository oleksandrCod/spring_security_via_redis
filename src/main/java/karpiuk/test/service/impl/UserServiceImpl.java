package karpiuk.test.service.impl;

import jakarta.transaction.Transactional;
import karpiuk.test.dto.LoggedInUserResponse;
import karpiuk.test.dto.RegistrationResponse;
import karpiuk.test.dto.UserRegistrationRequest;
import karpiuk.test.exception.exceptions.RegistrationException;
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

import java.util.List;
import java.util.Set;

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
        List<User> users = userRepository.findAll(pageable).getContent();
        List<LoggedInUserResponse> dtos = users.stream()
                .map(userMapper::toLoggedInResponseDto)
                .toList();
        log.info("Returning {} users.", dtos.size());
        return dtos;
    }

    @Override
    public LoggedInUserResponse getLoggedInUser() {
        java.lang.String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = serviceHelper.getUserByEmail(userEmail);
        return userMapper.toLoggedInResponseDto(user);
    }

    private void validateUniqueEmail(String email) throws RegistrationException {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new RegistrationException(REGISTRATION_ERROR_MESSAGE + email);
        }
    }

    private User createUserFromRequest(UserRegistrationRequest requestDto) {
        User user = new User();
        user.setEmail(requestDto.getEmail());
        user.setPassword(serviceHelper.encodePassword(requestDto.getPassword()));
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
}