package karpiuk.test.controller;

import karpiuk.test.dto.LoggedInUserInformationResponseDto;
import karpiuk.test.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public LoggedInUserInformationResponseDto getLoggedUser() {
        return userService.getLoggedInUser();
    }
}
