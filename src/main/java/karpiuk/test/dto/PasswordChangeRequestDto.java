package karpiuk.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import karpiuk.test.validation.password.FieldMatch;

@FieldMatch(field = "password",
        fieldMatch = "repeatPassword",
        message = "Input passwords must be equals")
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
