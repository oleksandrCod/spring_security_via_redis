package store.mybookstore.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import store.mybookstore.validation.email.EmailValidation;
import store.mybookstore.validation.password.FieldMatch;

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
