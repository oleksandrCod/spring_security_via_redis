package karpiuk.test.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record UserLoginRequest(
        @Email
        String email,
        @NotEmpty
        @Size(min = 4, max = 20)
        String password
) {
}
