package karpiuk.test.exception;

public class EmailConfirmationTokenException extends RuntimeException {
    public EmailConfirmationTokenException(String message) {
        super(message);
    }
}
