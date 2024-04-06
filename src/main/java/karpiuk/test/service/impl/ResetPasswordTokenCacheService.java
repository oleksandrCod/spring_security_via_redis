package karpiuk.test.service.impl;

import java.util.HashSet;
import java.util.Set;
import karpiuk.test.config.CacheConfiguration;
import karpiuk.test.exception.exceptions.InvalidPasswordResetToken;
import karpiuk.test.model.PasswordResetToken;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ResetPasswordTokenCacheService {
    private static final String RESET_TOKEN_ERROR_MESSAGE =
            "Provided reset token is invalid or expired!";
    private final Set<PasswordResetToken> passwordResetTokens = new HashSet<>();

    @CachePut(CacheConfiguration.EMAIL_CONFIRMATION_TOKEN_CACHE_NAME)
    public PasswordResetToken addToCache(PasswordResetToken passwordResetToken) {
        passwordResetTokens.add(passwordResetToken);
        return passwordResetToken;
    }

    @Cacheable(value = CacheConfiguration.EMAIL_CONFIRMATION_TOKEN_CACHE_NAME, unless = "#result == null")
    public PasswordResetToken getPasswordResetToken(String resetPasswordToken) {
        for (PasswordResetToken passwordResetToken : passwordResetTokens) {
            if (passwordResetToken.getResetPasswordToken().equals(resetPasswordToken)) {
                return passwordResetToken;
            }
        }
        throw new InvalidPasswordResetToken(RESET_TOKEN_ERROR_MESSAGE);
    }
}
