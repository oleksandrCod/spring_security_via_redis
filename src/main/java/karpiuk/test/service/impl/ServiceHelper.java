package karpiuk.test.service.impl;

import karpiuk.test.exception.exceptions.UserNotFoundException;
import karpiuk.test.model.User;
import karpiuk.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ServiceHelper {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final String USER_NOT_FOUND_ERROR_MESSAGE =
            "Can't find user by email,"
                    + " User may not register or logged in.";

    public User getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCaseAndFetchRoles(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE));
    }


    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
