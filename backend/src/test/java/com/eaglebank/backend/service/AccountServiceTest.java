package com.eaglebank.backend.service;

import com.eaglebank.backend.dto.BankAccountResponse;
import com.eaglebank.backend.exception.ResourceNotFoundException;
import com.eaglebank.backend.model.AccountType;
import com.eaglebank.backend.model.BankAccount;
import com.eaglebank.backend.model.User;
import com.eaglebank.backend.repository.BankAccountRepository;
import com.eaglebank.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private BankAccountRepository bankAccountRepository;
    private UserRepository userRepository;
    private AccountService accountService;
    private com.eaglebank.backend.repository.TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        bankAccountRepository = Mockito.mock(BankAccountRepository.class);
        userRepository = Mockito.mock(UserRepository.class);
        transactionRepository = Mockito.mock(com.eaglebank.backend.repository.TransactionRepository.class);
        accountService = new AccountService(bankAccountRepository, userRepository, transactionRepository);
    }

    private User user(String id) {
        return User.builder()
                .id(id)
                .name("Test User")
                .email("u@e.com")
                .phoneNumber("+441234567890")
                .password("pwd")
                .build();
    }

    private BankAccount account(String number, User owner) {
        return BankAccount.builder()
                .accountNumber(number)
                .sortCode("10-10-10")
                .name("Personal")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.ZERO)
                .currency("GBP")
                .user(owner)
                .build();
    }

    @Test
    void fetchAccount_returnsDetails_whenOwnedByUser() {
        String userId = "usr-123";
        User owner = user(userId);
        BankAccount acc = account("01000001", owner);
        when(bankAccountRepository.findById("01000001")).thenReturn(Optional.of(acc));

        BankAccountResponse resp = accountService.fetchAccount(userId, "01000001");

        assertThat(resp.getAccountNumber()).isEqualTo("01000001");
        assertThat(resp.getName()).isEqualTo("Personal");
        assertThat(resp.getAccountType()).isEqualTo(AccountType.PERSONAL);
    }

    @Test
    void fetchAccount_throws403_whenOwnedByDifferentUser() {
        String requesterId = "usr-aaa";
        User owner = user("usr-bbb");
        BankAccount acc = account("01000002", owner);
        when(bankAccountRepository.findById("01000002")).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.fetchAccount(requesterId, "01000002"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("The user is not allowed to access the bank account details");
    }

    @Test
    void fetchAccount_throws404_whenAccountMissing() {
        when(bankAccountRepository.findById("01999999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.fetchAccount("usr-x", "01999999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank account was not found");
    }

    @Test
    void deleteAccount_deletes_whenOwnedByUser() {
        String userId = "usr-123";
        User owner = user(userId);
        BankAccount acc = account("01000003", owner);
        when(bankAccountRepository.findById("01000003")).thenReturn(Optional.of(acc));

        accountService.deleteAccount(userId, "01000003");

        ArgumentCaptor<BankAccount> captor = ArgumentCaptor.forClass(BankAccount.class);
        verify(bankAccountRepository).delete(captor.capture());
        assertThat(captor.getValue().getAccountNumber()).isEqualTo("01000003");
    }

    @Test
    void deleteAccount_throws403_whenOwnedByDifferentUser() {
        String requesterId = "usr-aaa";
        User owner = user("usr-bbb");
        BankAccount acc = account("01000004", owner);
        when(bankAccountRepository.findById("01000004")).thenReturn(Optional.of(acc));

        assertThatThrownBy(() -> accountService.deleteAccount(requesterId, "01000004"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("The user is not allowed to delete the bank account details");
        verify(bankAccountRepository, never()).delete(any());
    }

    @Test
    void deleteAccount_throws404_whenAccountMissing() {
        when(bankAccountRepository.findById("01000005")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.deleteAccount("usr-y", "01000005"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Bank account was not found");
        verify(bankAccountRepository, never()).delete(any());
    }
}
