package com.eaglebank.backend.repository;

import com.eaglebank.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findAllByAccount_AccountNumberOrderByCreatedTimestampDesc(String accountNumber);
    Optional<Transaction> findByIdAndAccount_AccountNumber(String id, String accountNumber);
}
