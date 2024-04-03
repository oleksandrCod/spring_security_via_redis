package karpiuk.test.service;

import karpiuk.test.dto.LoggedInUserInformationResponseDto;
import karpiuk.test.dto.UserConfirmedRegistrationDto;
import karpiuk.test.dto.UserRegistrationRequestDto;
import karpiuk.test.dto.UserRegistrationResponseDto;

public interface UserService {
    UserRegistrationResponseDto register(UserRegistrationRequestDto requestDto);

    LoggedInUserInformationResponseDto getLoggedInUser();

    UserConfirmedRegistrationDto confirmEmail(String confirmationToke);
}
