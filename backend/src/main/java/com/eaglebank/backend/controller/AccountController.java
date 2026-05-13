package com.eaglebank.backend.controller;

import com.eaglebank.backend.dto.BankAccountResponse;
import com.eaglebank.backend.dto.CreateBankAccountRequest;
import com.eaglebank.backend.dto.ListBankAccountsResponse;
import com.eaglebank.backend.dto.UpdateBankAccountRequest;
import com.eaglebank.backend.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(@Valid @RequestBody CreateBankAccountRequest request,
                                                             Principal principal) {
        String userId = principal.getName();
        BankAccountResponse response = accountService.createAccount(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ListBankAccountsResponse> listAccounts(Principal principal) {
        String userId = principal.getName();
        ListBankAccountsResponse response = accountService.listAccounts(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<BankAccountResponse> fetchAccount(
            @jakarta.validation.constraints.Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$")
            @PathVariable("accountNumber") String accountNumber,
                                                            Principal principal) {
        String userId = principal.getName();
        BankAccountResponse response = accountService.fetchAccount(userId, accountNumber);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{accountNumber}")
    public ResponseEntity<BankAccountResponse> updateAccount(
            @jakarta.validation.constraints.Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$")
            @PathVariable("accountNumber") String accountNumber,
                                                             @Valid @RequestBody UpdateBankAccountRequest request,
                                                             Principal principal) {
        String userId = principal.getName();
        BankAccountResponse response = accountService.updateAccount(userId, accountNumber, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(
            @jakarta.validation.constraints.Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$")
            @PathVariable("accountNumber") String accountNumber,
                                              Principal principal) {
        String userId = principal.getName();
        accountService.deleteAccount(userId, accountNumber);
        return ResponseEntity.noContent().build();
    }

}
