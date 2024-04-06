package karpiuk.test.service;

import java.util.HashSet;
import java.util.Set;
import karpiuk.test.config.CacheConfiguration;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class BlackListingService {

    private final Set<String> blacklistedTokens = new HashSet<>();

    @CachePut(CacheConfiguration.JWT_BLACK_LIST_CACHE_NAME)
    public String blackListJwt(String jwt) {
        blacklistedTokens.add(jwt);
        return jwt;
    }

    @Cacheable(value = CacheConfiguration.JWT_BLACK_LIST_CACHE_NAME, unless = "#result == null")
    public String getJwtBlackList(String jwt) {
        return blacklistedTokens.contains(jwt) ? jwt : null;
    }
}
