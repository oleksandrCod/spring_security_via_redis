package store.mybookstore.validation.email;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class EmailValidationImpl implements ConstraintValidator<EmailValidation, String> {
    private static final String EMAIL_PATTERN = "^(.+)@(\\S+)$";

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return email != null && Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }
}
