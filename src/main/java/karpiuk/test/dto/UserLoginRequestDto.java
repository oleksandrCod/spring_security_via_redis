package karpiuk.test.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import karpiuk.test.validation.email.EmailValidation;

public record UserLoginRequestDto(
        @EmailValidation
        String email,
        @NotEmpty
        @Size(min = 4, max = 20)
        String password
) {
}
