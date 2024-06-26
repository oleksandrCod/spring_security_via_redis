package karpiuk.test.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @Email
        String email,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName) {

}
