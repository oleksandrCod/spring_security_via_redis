package karpiuk.test.service;

import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;
import karpiuk.test.exception.RegistrationException;
import karpiuk.test.model.User;

public interface UserService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto) throws RegistrationException;

    User getLoggedInUser();
}
