package karpiuk.test.solanawallet;

import org.sol4k.Connection;
import org.sol4k.PublicKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolanaConfiguration {
    @Value("${solana.api.url}")
    private String solanaApiUrl;

    @Value("${solana.public.key}")
    private String solanaPublicKey;

    @Bean
    public Connection getConnection() {
        return new Connection(solanaApiUrl);
    }

    @Bean
    public PublicKey getPublicKey() {
        return new PublicKey(solanaPublicKey);
    }
}
