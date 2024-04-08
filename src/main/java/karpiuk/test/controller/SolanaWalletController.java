package karpiuk.test.controller;

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
public class SolanaWalletController {
    private final SolanaWalletService solanaWalletService;

    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponseDto> getWalletBalance() {
        return ResponseEntity.ok(solanaWalletService.printWalletBalance());
    }

    @GetMapping("/airdrop")
    public ResponseEntity<AirDropResponseDto> requestAirDrop(@RequestParam("amount") Long amount) {
        return ResponseEntity.ok(solanaWalletService.requestAirDrop(amount));
    }

    @PostMapping("send")
    public ResponseEntity<SendingTransactionResponseDto> sendSolToAccount(
            @RequestParam("amount") Long amount, @RequestParam("receiverKey") String receiverKey) {
        return ResponseEntity.ok(solanaWalletService.solTransfer(amount, receiverKey));
    }
}
