package karpiuk.test.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequestDto(
        @Email
        String email,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName) {

}
