package karpiuk.test.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class PasswordResetToken {
    private String resetPasswordToken;
    private User user;
    private Instant createdDate;

    public PasswordResetToken(User user) {
        this.user = user;
        createdDate = Instant.now();
        resetPasswordToken = UUID.randomUUID().toString();
    }
}
