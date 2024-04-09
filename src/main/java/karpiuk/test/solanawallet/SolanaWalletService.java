package karpiuk.test.solanawallet;

import java.security.KeyPair;
import karpiuk.test.dto.response.LoggedInUserResponse;
import karpiuk.test.dto.solana.AirDropResponseDto;
import karpiuk.test.dto.solana.SendingTransactionResponseDto;
import karpiuk.test.dto.solana.WalletBalanceResponseDto;
import karpiuk.test.model.User;
import karpiuk.test.service.UserService;
import karpiuk.test.service.impl.ServiceHelper;
import lombok.RequiredArgsConstructor;
import org.sol4k.Base58;
import org.sol4k.Connection;
import org.sol4k.Keypair;
import org.sol4k.PublicKey;
import org.sol4k.Transaction;
import org.sol4k.instruction.TransferInstruction;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SolanaWalletService {
    private final ServiceHelper serviceHelper;
    private final Connection connection;

    public WalletBalanceResponseDto printWalletBalanceOfCurrentUser() {
        User user = fetchCurrentUser();

        String privetKey = user.getPrivateSolanaKey();
        PublicKey publicKey = Keypair.fromSecretKey(Base58.decode(privetKey)).getPublicKey();

        var balance = connection.getBalance(publicKey);
        return new WalletBalanceResponseDto("Your balance is :", balance);
    }

    public AirDropResponseDto requestAirDrop(Long amount) {
        connection.requestAirdrop(publicKey, amount);
        return new AirDropResponseDto("Received airdrop amount:", amount);
    }

    public SendingTransactionResponseDto solTransfer(Long amount, String receiverKey) {
        var connection = new Connection("https://api.devnet.solana.com");
        var blockhash = connection.getLatestBlockhash();
        byte[] decode = Base58.decode("2WGcYYau2gLu2DUq68SxxXQmCgi77n8hFqqLNbNyg6Xfh2m3tvg8LF5Lgh69CFDux41LUKV1ak1ERHUqiBZnyshz");
        var sender = Keypair.fromSecretKey(decode);
        var receiver = new PublicKey(receiverKey);
        var instruction = new TransferInstruction(sender.getPublicKey(), receiver, 1000);
        var transaction = new Transaction(
                blockhash,
                instruction,
                sender.getPublicKey()
        );
        transaction.sign(sender);
        var signature = connection.sendTransaction(transaction);
        return new SendingTransactionResponseDto("Tokens send successfully!", amount, receiverKey);
    }

    private User fetchCurrentUser() {
        return serviceHelper.getUserByEmail(SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName());
    }
}
