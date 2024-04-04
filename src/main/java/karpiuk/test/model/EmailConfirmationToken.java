package karpiuk.test.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class EmailConfirmationToken {
    private String confirmationToken;
    private Instant createdDate;
    private User user;

    public EmailConfirmationToken(User user) {
        this.user = user;
        createdDate = Instant.now();
        confirmationToken = UUID.randomUUID().toString();
    }
}
