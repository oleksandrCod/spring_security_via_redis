package karpiuk.test.service.impl;

import java.util.HashSet;
import java.util.Set;
import karpiuk.test.config.CacheConfiguration;
import karpiuk.test.exception.EmailConfirmationTokenException;
import karpiuk.test.model.EmailConfirmationToken;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EmailTokenCacheService {
    private static final String CONFIRMATION_TOKEN_ERROR_MESSAGE =
            "Provided confirmation token is invalid or expired!";
    private final Set<EmailConfirmationToken> emailConfirmationTokens = new HashSet<>();

    @CachePut(CacheConfiguration.EMAIL_CONFIRMATION_TOKEN_CACHE_NAME)
    public EmailConfirmationToken addToCache(EmailConfirmationToken emailToken) {
        emailConfirmationTokens.add(emailToken);
        return emailToken;
    }

    @Cacheable(value = CacheConfiguration.EMAIL_CONFIRMATION_TOKEN_CACHE_NAME,
            unless = "#result == null")
    public EmailConfirmationToken getEmailToken(String emailToken) {
        for (EmailConfirmationToken emailConfirmationToken : emailConfirmationTokens) {
            if (emailConfirmationToken.getConfirmationToken().equals(emailToken)) {
                return emailConfirmationToken;
            }
        }
        throw new EmailConfirmationTokenException(CONFIRMATION_TOKEN_ERROR_MESSAGE);
    }
}

