package com.eaglebank.backend.repository;

import com.eaglebank.backend.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    boolean existsByUser_Id(String userId);

    List<BankAccount> findAllByUser_Id(String userId);
    BankAccount findByAccountNumber(String accountNumber);
}
