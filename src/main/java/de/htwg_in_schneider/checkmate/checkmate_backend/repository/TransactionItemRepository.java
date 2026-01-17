package de.htwg_in_schneider.checkmate.checkmate_backend.repository;

import de.htwg_in_schneider.checkmate.checkmate_backend.model.TransactionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionItemRepository extends JpaRepository<TransactionItem, Long> {
}