package karpiuk.test.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import karpiuk.test.validation.password.FieldMatch;
import lombok.Data;

@Data
@FieldMatch(field = "password",
        fieldMatch = "repeatPassword",
        message = "Input passwords must be equals")
public class UserRegistrationRequest {
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Size(min = 6, max = 100)
    private String repeatPassword;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;
}
