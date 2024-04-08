package karpiuk.test.dto.solana;

import java.math.BigInteger;

public record WalletBalanceResponseDto(String message, BigInteger amount) {
}
