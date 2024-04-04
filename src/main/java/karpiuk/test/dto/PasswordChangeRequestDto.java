package karpiuk.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequestDto(
        @NotBlank
        @Size(min = 6, max = 100)
        String password,

        @NotBlank
        @Size(min = 6, max = 100)
        String repeatPassword,

        @NotBlank
        String resetToken
) {
}
