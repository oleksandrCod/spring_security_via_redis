package karpiuk.test.solanawallet;

import org.sol4k.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolanaConfiguration {

    @Value("${solana.api.url}")
    private String solanaApiUrl;

    @Bean
    public Connection getConnection() {
        return new Connection(solanaApiUrl);
    }
}
