package karpiuk.test.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import karpiuk.test.model.User;
import org.springframework.stereotype.Component;

@Component
public class HashProvider {
    public String hashToSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to calculate SHA-256 hash", e);
        }
    }

    public String hashToSha256(User input) {
        return hashToSha256(input.toString());
    }
}