package karpiuk.test.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import karpiuk.test.validation.email.EmailValidation;
import karpiuk.test.validation.password.FieldMatch;
import lombok.Data;

@Data
@FieldMatch(field = "password",
        fieldMatch = "repeatPassword",
        message = "Input passwords must be equals")
public class UserRegistrationRequestDto {
    @EmailValidation
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
    @NotBlank
    private String shippingAddress;
}
