package com.eaglebank.backend.controller;

import com.eaglebank.backend.dto.TransactionResponse;
import com.eaglebank.backend.exception.GlobalExceptionHandler;
import com.eaglebank.backend.exception.InsufficientFundsException;
import com.eaglebank.backend.exception.ResourceNotFoundException;
import com.eaglebank.backend.model.TransactionType;
import com.eaglebank.backend.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransactionsControllerTest {

    private TransactionService transactionService;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        transactionService = Mockito.mock(TransactionService.class);
        TransactionsController transactionsController = new TransactionsController(transactionService);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionsController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void fetchTransaction_200_ok() throws Exception {
        Mockito.when(transactionService.fetchTransaction(anyString(), anyString(), anyString()))
                .thenReturn(sampleTransactionResponse());

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", "01012345", "tan-abc1234567")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("tan-abc1234567"))
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.type").value("deposit"));
    }

    @Test
    void listTransactions_404_notFound_whenAccountDoesNotExist() throws Exception {
        Mockito.when(transactionService.listTransactions(anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Bank account was not found"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions", "01999999")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Bank account was not found"));
    }

    @Test
    void fetchTransaction_403_forbidden_whenAccessingAnotherUsersAccount() throws Exception {
        Mockito.when(transactionService.fetchTransaction(anyString(), anyString(), anyString()))
                .thenThrow(new AccessDeniedException("The user is not allowed to access the transaction"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", "01012345", "tan-abc1234567")
                        .principal(principal("usr-other"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("The user is not allowed to access the transaction"));
    }

    @Test
    void fetchTransaction_404_notFound_whenAccountDoesNotExist() throws Exception {
        Mockito.when(transactionService.fetchTransaction(anyString(), anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Bank account was not found"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", "01999999", "tan-abc1234567")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Bank account was not found"));
    }

    @Test
    void fetchTransaction_404_notFound_whenTransactionDoesNotExist() throws Exception {
        Mockito.when(transactionService.fetchTransaction(anyString(), anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Transaction was not found"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", "01012345", "tan-missing1234")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Transaction was not found"));
    }

    @Test
    void fetchTransaction_404_notFound_whenTransactionNotAssociatedWithAccount() throws Exception {
        Mockito.when(transactionService.fetchTransaction(anyString(), anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Transaction was not found"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions/{transactionId}", "01012345", "tan-otheracct1")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Transaction was not found"));
    }

    @Test
    void createTransaction_422_insufficientFunds() throws Exception {
        Mockito.when(transactionService.createTransaction(anyString(), anyString(), Mockito.any()))
                .thenThrow(new InsufficientFundsException("Insufficient funds to process transaction"));

        String body = "{\n" +
                "  \"amount\": 100.00,\n" +
                "  \"currency\": \"GBP\",\n" +
                "  \"type\": \"WITHDRAWAL\"\n" +
                "}";

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", "01012345")
                        .principal(principal("usr-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Insufficient funds to process transaction"));
    }

    @Test
    void createTransaction_404_notFound() throws Exception {
        Mockito.when(transactionService.createTransaction(anyString(), anyString(), Mockito.any()))
                .thenThrow(new ResourceNotFoundException("Bank account was not found"));

        String body = "{\n" +
                "  \"amount\": 50.00,\n" +
                "  \"currency\": \"GBP\",\n" +
                "  \"type\": \"DEPOSIT\"\n" +
                "}";

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", "01999999")
                        .principal(principal("usr-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Bank account was not found"));
    }

    @Test
    void createTransaction_400_missingRequiredFields_returnsBadRequestWithDetails() throws Exception {
        // Missing amount and type; only currency provided
        String body = "{\n" +
                "  \"currency\": \"GBP\"\n" +
                "}";

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", "01012345")
                        .principal(principal("usr-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[?(@.field=='amount')]").isNotEmpty())
                .andExpect(jsonPath("$.details[?(@.field=='type')]").isNotEmpty())
                .andExpect(jsonPath("$.details[?(@.field=='amount')].[*].type").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("required"))))
                .andExpect(jsonPath("$.details[?(@.field=='type')].[*].type").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.is("required"))));
    }

    @Test
    void createTransaction_400_invalidCurrency_patternViolation_returnsBadRequestWithDetails() throws Exception {
        // Invalid currency (must be GBP)
        String body = "{\n" +
                "  \"amount\": 50.00,\n" +
                "  \"currency\": \"USD\",\n" +
                "  \"type\": \"DEPOSIT\"\n" +
                "}";

        mockMvc.perform(post("/v1/accounts/{accountNumber}/transactions", "01012345")
                        .principal(principal("usr-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.details[0].field").value("currency"))
                .andExpect(jsonPath("$.details[0].type").value("pattern"));
    }

    @Test
    void listTransactions_403_forbidden_whenAccessingAnotherUsersAccount() throws Exception {
        Mockito.when(transactionService.listTransactions(anyString(), anyString()))
                .thenThrow(new AccessDeniedException("The user is not allowed to access the transactions"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}/transactions", "01012345")
                        .principal(principal("usr-abc"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("The user is not allowed to access the transactions"));
    }

    private TransactionResponse sampleTransactionResponse() {
        return TransactionResponse.builder()
                .id("tan-abc1234567")
                .amount(new java.math.BigDecimal("50.00"))
                .currency("GBP")
                .type(TransactionType.DEPOSIT)
                .reference("Initial deposit")
                .userId("usr-123")
                .build();
    }

    private Principal principal(String userId) {
        return () -> userId;
    }
}
