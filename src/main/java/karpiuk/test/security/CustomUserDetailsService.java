package karpiuk.test.security;

import karpiuk.test.exception.exceptions.UserNotFoundException;
import karpiuk.test.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomUserDetailsService implements UserDetailsService {
    private static final String USER_NOT_FOUND_ERROR_MESSAGE = "Can't find user by email: ";
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmailIgnoreCaseAndFetchRoles(username)
                .orElseThrow(() -> new UserNotFoundException(
                        USER_NOT_FOUND_ERROR_MESSAGE + username));
    }
}
