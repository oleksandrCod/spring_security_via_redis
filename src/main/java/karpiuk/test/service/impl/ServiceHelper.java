package karpiuk.test.service.impl;

import karpiuk.test.exception.exceptions.UserNotFoundException;
import karpiuk.test.model.User;
import karpiuk.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceHelper {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_NOT_FOUND_ERROR_MESSAGE =
            "Can't find user by email,"
                    + " User may not register or logged in.";

    public User getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);

        User user = userRepository.findByEmailIgnoreCaseAndFetchRoles(email)
                .orElseThrow(() -> {

                    log.error("User not found with email: {}", email);

                    return new UserNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE);
                });
        log.info("User fetched successfully for email: {}", email);

        return user;
    }

    public String encodePassword(String password) {
        log.info("Encoding password");

        String encodedPassword = passwordEncoder.encode(password);

        log.info("Password encoded successfully");

        return encodedPassword;
    }
}
