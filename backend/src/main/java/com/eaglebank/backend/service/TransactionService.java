package com.eaglebank.backend.service;

import com.eaglebank.backend.dto.CreateTransactionRequest;
import com.eaglebank.backend.dto.ListTransactionsResponse;
import com.eaglebank.backend.dto.TransactionResponse;
import com.eaglebank.backend.exception.ResourceNotFoundException;
import com.eaglebank.backend.model.BankAccount;
import com.eaglebank.backend.model.Transaction;
import com.eaglebank.backend.model.TransactionType;
import com.eaglebank.backend.repository.BankAccountRepository;
import com.eaglebank.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private static final String DEFAULT_CURRENCY = "GBP";

    private final BankAccountRepository bankAccountRepository;
    private final TransactionRepository transactionRepository;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public TransactionResponse createTransaction(String userId, String accountNumber, CreateTransactionRequest request) {
        BankAccount account = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found"));

        if (account.getUser() == null || account.getUser().getId() == null || !account.getUser().getId().equals(userId)) {
            // Follow transaction 403 wording from spec
            throw new AccessDeniedException("The user is not allowed to access the transaction");
        }

        if (!DEFAULT_CURRENCY.equals(account.getCurrency())) {
            throw new IllegalArgumentException("Unsupported account currency");
        }
        if (!DEFAULT_CURRENCY.equals(request.getCurrency())) {
            throw new IllegalArgumentException("currency must be GBP");
        }

        BigDecimal amount = request.getAmount();
        if (amount == null) {
            throw new IllegalArgumentException("amount is required");
        }

        TransactionType type = request.getType();
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }

        // Compute new balance with validation
        BigDecimal newBalance;
        if (type == TransactionType.DEPOSIT) {
            newBalance = account.getBalance().add(amount);
            // Enforce domain upper bound per spec (balance max 10000.00)
            if (newBalance.compareTo(new BigDecimal("10000.00")) > 0) {
                throw new IllegalArgumentException("Balance cannot exceed 10000.00");
            }
        } else { // WITHDRAWAL
            if (account.getBalance().compareTo(amount) < 0) {
                throw new com.eaglebank.backend.exception.InsufficientFundsException("Insufficient funds to process transaction");
            }
            newBalance = account.getBalance().subtract(amount);
        }

        account.setBalance(newBalance);

        Transaction tx = Transaction.builder()
                .id(generateTransactionId())
                .amount(amount)
                .currency(DEFAULT_CURRENCY)
                .type(type)
                .reference(request.getReference())
                .account(account)
                .createdTimestamp(LocalDateTime.now())
                .build();

        transactionRepository.saveAndFlush(tx);
        bankAccountRepository.saveAndFlush(account);

        return mapToResponse(tx, account);
    }

    @Transactional(readOnly = true)
    public ListTransactionsResponse listTransactions(String userId, String accountNumber) {
        BankAccount account = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found"));

        if (account.getUser() == null || account.getUser().getId() == null || !account.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("The user is not allowed to access the transactions");
        }

        List<Transaction> txs = transactionRepository
                .findAllByAccount_AccountNumberOrderByCreatedTimestampDesc(accountNumber);

        List<TransactionResponse> items = txs.stream()
                .map(t -> mapToResponse(t, account))
                .collect(Collectors.toList());

        return ListTransactionsResponse.builder()
                .transactions(items)
                .build();
    }

    @Transactional(readOnly = true)
    public TransactionResponse fetchTransaction(String userId, String accountNumber, String transactionId) {
        BankAccount account = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found"));

        if (account.getUser() == null || account.getUser().getId() == null || !account.getUser().getId().equals(userId)) {
            // For fetch transaction use singular wording per OpenAPI
            throw new org.springframework.security.access.AccessDeniedException("The user is not allowed to access the transaction");
        }

        Transaction tx = transactionRepository
                .findByIdAndAccount_AccountNumber(transactionId, accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction was not found"));

        return mapToResponse(tx, account);
    }

    private TransactionResponse mapToResponse(Transaction t, BankAccount account) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .type(t.getType())
                .reference(t.getReference())
                .userId(account.getUser() != null ? account.getUser().getId() : null)
                .balance(account.getBalance())
                .createdTimestamp(t.getCreatedTimestamp())
                .build();
    }

    private String generateTransactionId() {
        // tan- followed by 6 random base62 chars (matches OpenAPI ^tan-[A-Za-z0-9]{6}$)
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder("tan-");
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
