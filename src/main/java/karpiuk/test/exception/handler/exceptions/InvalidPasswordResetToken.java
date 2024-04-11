package karpiuk.test.exception.handler.exceptions;

public class InvalidPasswordResetToken extends RuntimeException {
    public InvalidPasswordResetToken(String message) {
        super(message);
    }
}
