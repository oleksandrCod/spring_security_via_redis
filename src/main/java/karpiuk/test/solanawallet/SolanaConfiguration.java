package karpiuk.test.solanawallet;

import org.sol4k.Connection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolanaConfiguration {

    @Bean
    public Connection getConnection(
            @Value("${solana.api.url}")
            String solanaApiUrl) {
        return new Connection(solanaApiUrl);
    }
}
