package karpiuk.test.solanawallet;

import karpiuk.test.dto.solana.AirDropResponseDto;
import karpiuk.test.dto.solana.SendingTransactionResponseDto;
import karpiuk.test.dto.solana.WalletBalanceResponseDto;
import karpiuk.test.model.User;
import karpiuk.test.service.impl.ServiceHelper;
import org.sol4k.Base58;
import org.sol4k.Connection;
import org.sol4k.Keypair;
import org.sol4k.PublicKey;
import org.sol4k.Transaction;
import org.sol4k.instruction.TransferInstruction;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SolanaWalletService {
    private final ServiceHelper serviceHelper;
    private final Connection connection;
    private User user;
    private PublicKey userPublicKey;
    private Keypair sender;

    public SolanaWalletService(
            ServiceHelper serviceHelper,
            Connection connection
    ) {

        this.serviceHelper = serviceHelper;
        this.connection = connection;
    }

    public WalletBalanceResponseDto printWalletBalanceOfCurrentUser() {
        initUser();
        var balance = connection.getBalance(userPublicKey);
        return new WalletBalanceResponseDto("Your balance is :", balance);
    }

    public AirDropResponseDto requestAirDrop(Long amount) {
        initUser();
        connection.requestAirdrop(userPublicKey, amount);
        return new AirDropResponseDto("Received airdrop amount:", amount);
    }

    public SendingTransactionResponseDto solTransfer(Long amount, String receiverKey) {
        initUser();
        var blockhash = connection.getLatestBlockhash();

        var receiver = new PublicKey(receiverKey);
        var instruction = new TransferInstruction(userPublicKey, receiver, amount);

        var transaction = new Transaction(blockhash, instruction, userPublicKey);

        transaction.sign(sender);

        String signature = connection.sendTransaction(transaction);

        return new SendingTransactionResponseDto(
                String.format("Tokens send successfully! Signature: %s", signature), amount, receiverKey);
    }

    private void initUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            this.user = serviceHelper.getUserByEmail(authentication.getName());
            this.sender = Keypair.fromSecretKey(Base58.decode(user.getPrivateSolanaKey()));
            this.userPublicKey = sender.getPublicKey();
        }
    }
}

