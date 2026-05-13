package com.eaglebank.backend.controller;

import com.eaglebank.backend.dto.CreateTransactionRequest;
import com.eaglebank.backend.dto.ListTransactionsResponse;
import com.eaglebank.backend.dto.TransactionResponse;
import com.eaglebank.backend.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionsController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
                                                                 @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$")
                                                                 @PathVariable("accountNumber") String accountNumber,
                                                                 @Valid @RequestBody CreateTransactionRequest request,
                                                                 Principal principal) {
        String userId = principal.getName();
        TransactionResponse response = transactionService.createTransaction(userId, accountNumber, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ListTransactionsResponse> listTransactions(
                                                                     @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$")
                                                                     @PathVariable("accountNumber") String accountNumber,
                                                                     Principal principal) {
        String userId = principal.getName();
        ListTransactionsResponse response = transactionService.listTransactions(userId, accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> fetchTransaction(
                                                                @Pattern(regexp = "^01\\d{6}$", message = "accountNumber must match ^01\\d{6}$")
                                                                @PathVariable("accountNumber") String accountNumber,
                                                                @Pattern(regexp = "^tan-[A-Za-z0-9]{6}$", message = "transactionId must match ^tan-[A-Za-z0-9]{6}$")
                                                                @PathVariable("transactionId") String transactionId,
                                                                Principal principal) {
        String userId = principal.getName();
        TransactionResponse response = transactionService.fetchTransaction(userId, accountNumber, transactionId);
        return ResponseEntity.ok(response);
    }
}
