package karpiuk.test.dto.solona;

import java.math.BigInteger;

public record SendingTransactionResponseDto(String message, Long amount, String receiverKey) {
}
