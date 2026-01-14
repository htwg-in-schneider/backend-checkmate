package de.htwg_in_schneider.checkmate.checkmate_backend.controller;

import de.htwg_in_schneider.checkmate.checkmate_backend.dto.CreateTransactionRequest;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.Transaction;
import de.htwg_in_schneider.checkmate.checkmate_backend.model.TransactionItem;
import de.htwg_in_schneider.checkmate.checkmate_backend.repository.TransactionRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateTransactionRequest req
    ) {
        // buyerOauthId ist Auth0 sub
        String buyerOauthId = jwt.getSubject();

        Transaction tx = new Transaction();
        tx.setBuyerOauthId(buyerOauthId);

        // wenn buyerEmail nicht mitgeschickt wird, nimm aus Token (falls vorhanden)
        String emailFromToken = jwt.getClaimAsString("email");
        tx.setBuyerEmail(req.getBuyerEmail() != null ? req.getBuyerEmail() : (emailFromToken != null ? emailFromToken : "unknown@example.com"));

        double total = 0.0;

        for (var itemDto : req.getItems()) {
            TransactionItem item = new TransactionItem();
            item.setTutorId(itemDto.getTutorId());
            item.setTutorName(itemDto.getTutorName());
            item.setStartAt(itemDto.getStartAt());
            item.setDurationMinutes(itemDto.getDurationMinutes());
            item.setPrice(itemDto.getPrice());

            total += itemDto.getPrice();
            tx.addItem(item);
        }

        tx.setTotalPrice(total);

        Transaction saved = transactionRepository.save(tx);

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "transactionId", saved.getId(),
                "totalPrice", saved.getTotalPrice()
        ));
    }

    // Optional: Student kann eigene Transaktionen sehen
    @GetMapping("/mine")
    public ResponseEntity<?> myTransactions(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(transactionRepository.findByBuyerOauthIdOrderByCreatedAtDesc(jwt.getSubject()));
    }
}