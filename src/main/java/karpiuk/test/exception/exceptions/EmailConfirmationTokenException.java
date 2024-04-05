package karpiuk.test.exception.exceptions;

public class EmailConfirmationTokenException extends RuntimeException {
    public EmailConfirmationTokenException(String message) {
        super(message);
    }
}
