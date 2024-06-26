package karpiuk.test.security;

import karpiuk.test.exception.handler.exceptions.UserNotFoundException;
import karpiuk.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomUserDetailsService implements UserDetailsService {
    private static final String USER_NOT_FOUND_ERROR_MESSAGE = "Can't find user by email: ";
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("Attempting to load user by username: {}", username);

        UserDetails userDetails = userRepository
                .findByEmailIgnoreCaseAndFetchRoles(username).orElseThrow(() -> {
                    log.error(USER_NOT_FOUND_ERROR_MESSAGE + "{}", username);
                    return new UserNotFoundException(USER_NOT_FOUND_ERROR_MESSAGE + username);
                });

        log.info("User loaded successfully: {}", username);

        return userDetails;
    }
}
