package karpiuk.test.exception.handler.exceptions;

public class EmailConfirmationTokenException extends RuntimeException {
    public EmailConfirmationTokenException(String message) {
        super(message);
    }
}
