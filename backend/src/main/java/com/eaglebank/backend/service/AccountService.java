package com.eaglebank.backend.service;

import com.eaglebank.backend.dto.BankAccountResponse;
import com.eaglebank.backend.dto.CreateBankAccountRequest;
import com.eaglebank.backend.dto.UpdateBankAccountRequest;
import com.eaglebank.backend.dto.ListBankAccountsResponse;
import com.eaglebank.backend.exception.ResourceNotFoundException;
import com.eaglebank.backend.model.BankAccount;
import com.eaglebank.backend.model.User;
import com.eaglebank.backend.repository.BankAccountRepository;
import com.eaglebank.backend.repository.UserRepository;
import com.eaglebank.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final String DEFAULT_SORT_CODE = "10-10-10";
    private static final String DEFAULT_CURRENCY = "GBP";

    private final BankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;
    // Keep TransactionRepository to preserve constructor signature for existing tests (unused here after refactor)
    //private final TransactionRepository transactionRepository;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public BankAccountResponse createAccount(String userId, CreateBankAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accountNumber = generateUniqueAccountNumber();

        BankAccount account = BankAccount.builder()
                .accountNumber(accountNumber)
                .sortCode(DEFAULT_SORT_CODE)
                .name(request.getName())
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .currency(DEFAULT_CURRENCY)
                .user(user)
                .build();

        BankAccount saved = bankAccountRepository.saveAndFlush(account);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public ListBankAccountsResponse listAccounts(String userId) {
        List<BankAccount> accounts = bankAccountRepository.findAllByUser_Id(userId);
        List<BankAccountResponse> items = accounts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ListBankAccountsResponse.builder()
                .accounts(items)
                .build();
    }

    @Transactional(readOnly = true)
    public BankAccountResponse fetchAccount(String userId, String accountNumber) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new ResourceNotFoundException("Bank account was not found");
        }

        if (account.getUser() == null || account.getUser().getId() == null || !account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("The user is not allowed to access the bank account details");
        }

        return mapToResponse(account);
    }

    @Transactional
    public void deleteAccount(String userId, String accountNumber) {
        BankAccount account = bankAccountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new ResourceNotFoundException("Bank account was not found");
        }

        if (account.getUser() == null || account.getUser().getId() == null || !account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("The user is not allowed to delete the bank account details");
        }

        bankAccountRepository.delete(account);
    }

    @Transactional
    public BankAccountResponse updateAccount(String userId, String accountNumber, UpdateBankAccountRequest request) {
        BankAccount account = bankAccountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found"));

        if (account.getUser() == null || account.getUser().getId() == null || !account.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("The user is not allowed to access the bank account details");
        }

        if (request.getName() != null) {
            account.setName(request.getName());
        }
        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
        }

        BankAccount saved = bankAccountRepository.saveAndFlush(account);
        return mapToResponse(saved);
    }


    private String generateUniqueAccountNumber() {
        // Must match ^01\d{6}$
        for (int i = 0; i < 1000; i++) {
            int suffix = random.nextInt(1_000_000); // 0..999999
            String candidate = String.format("01%06d", suffix);
            if (!bankAccountRepository.existsById(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Unable to generate unique account number");
    }

    private BankAccountResponse mapToResponse(BankAccount a) {
        return BankAccountResponse.builder()
                .accountNumber(a.getAccountNumber())
                .sortCode(a.getSortCode())
                .name(a.getName())
                .accountType(a.getAccountType())
                .balance(a.getBalance())
                .currency(a.getCurrency())
                .createdTimestamp(a.getCreatedTimestamp())
                .updatedTimestamp(a.getUpdatedTimestamp())
                .build();
    }

}
