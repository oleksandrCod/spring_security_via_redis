package karpiuk.test.service;

import java.util.List;
import karpiuk.test.dto.request.UserRegistrationRequest;
import karpiuk.test.dto.response.LoggedInUserResponse;
import karpiuk.test.dto.response.RegistrationResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    RegistrationResponse register(UserRegistrationRequest requestDto);

    LoggedInUserResponse getLoggedInUser();

    List<LoggedInUserResponse> getAllUsers(Pageable pageable);

}
