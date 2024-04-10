package karpiuk.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import karpiuk.test.dto.solana.AirDropResponseDto;
import karpiuk.test.dto.solana.SendingTransactionResponseDto;
import karpiuk.test.dto.solana.WalletBalanceResponseDto;
import karpiuk.test.solanawallet.SolanaWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/solana")
@RequiredArgsConstructor
@Tag(name = "Solana wallet management",
        description = "Provide endpoints for logged-in users, to manage requests with user Solana wallet.")
public class SolanaWalletController {
    private final SolanaWalletService solanaWalletService;

    @GetMapping("/balance")
    @Operation(summary = "Get balance endpoint",
            description = "Provide flow for retrieving balance of user wallet. "
                    + "Return message with SOL amount on user wallet.")
    public ResponseEntity<WalletBalanceResponseDto> getWalletBalance() {
        return ResponseEntity.ok(solanaWalletService.printWalletBalanceOfCurrentUser());
    }

    @GetMapping("/airdrop")
    @Operation(summary = "Airdrop requesting endpoint.",
            description = "Provide flow for requesting airdrop in SOL, receive amount of SOL. "
                    + "Return message with with airdrop amount.")
    public ResponseEntity<AirDropResponseDto> requestAirDrop(@RequestParam("amount") Long amount) {
        return ResponseEntity.ok(solanaWalletService.requestAirDrop(amount));
    }

    @PostMapping("send")
    @Operation(summary = "Send SOL endpoint",
            description = "Provide flow for sending special amount fo SOL to another account, "
                    + "receive request with receiver key and SOL amount for transaction. "
                    + "Return transaction signature with SOL amount for transfer.")
    public ResponseEntity<SendingTransactionResponseDto> sendSolToAccount(
            @RequestParam("amount") Long amount, @RequestParam("receiverKey") String receiverKey) {
        return ResponseEntity.ok(solanaWalletService.solTransfer(amount, receiverKey));
    }
}
