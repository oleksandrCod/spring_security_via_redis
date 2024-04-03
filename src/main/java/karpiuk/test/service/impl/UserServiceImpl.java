package karpiuk.test.service.impl;

import java.util.Set;
import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;
import karpiuk.test.exception.RegistrationException;
import karpiuk.test.mapper.UserMapper;
import karpiuk.test.model.Role;
import karpiuk.test.model.User;
import karpiuk.test.repository.RoleRepository;
import karpiuk.test.repository.UserRepository;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${security.admin.email")
    private String adminEmail;

    @Override
    public UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto)
            throws RegistrationException {
        if (userRepository.findUserByEmail(requestDto.getEmail()).isPresent()) {
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
        return userMapper.toResponseDto((userRepository.save(user)));
    }

    @Override
    public User getLoggedInUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(userEmail)
                .orElseThrow(
                        () -> new UsernameNotFoundException("Can't find user by email,"
                                + " User may not register."));
    }
}
