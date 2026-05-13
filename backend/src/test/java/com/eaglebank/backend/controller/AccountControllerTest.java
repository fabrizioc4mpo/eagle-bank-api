package com.eaglebank.backend.controller;

import com.eaglebank.backend.dto.BankAccountResponse;
import com.eaglebank.backend.exception.GlobalExceptionHandler;
import com.eaglebank.backend.exception.ResourceNotFoundException;
import com.eaglebank.backend.model.AccountType;
import com.eaglebank.backend.service.AccountService;
import org.springframework.security.access.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.security.Principal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

class AccountControllerTest {

    private AccountService accountService;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        accountService = Mockito.mock(AccountService.class);
        AccountController accountController = new AccountController(accountService);
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Principal principal(String userId) {
        return () -> userId;
    }

    private BankAccountResponse sampleResponse() {
        return BankAccountResponse.builder()
                .accountNumber("01012345")
                .sortCode("10-10-10")
                .name("Personal")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.ZERO)
                .currency("GBP")
                .build();
    }

    @Test
    void fetchAccount_200_ok() throws Exception {
        when(accountService.fetchAccount(anyString(), anyString())).thenReturn(sampleResponse());

        mockMvc.perform(get("/v1/accounts/{accountNumber}", "01012345")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value("01012345"))
                .andExpect(jsonPath("$.sortCode").value("10-10-10"))
                .andExpect(jsonPath("$.accountType").value("personal"));
    }

    @Test
    void fetchAccount_404_notFound() throws Exception {
        when(accountService.fetchAccount(anyString(), anyString()))
                .thenThrow(new ResourceNotFoundException("Bank account was not found"));

        mockMvc.perform(get("/v1/accounts/{accountNumber}", "01999999")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Bank account was not found"));
    }

    @Test
    void deleteAccount_204_noContent() throws Exception {
        mockMvc.perform(delete("/v1/accounts/{accountNumber}", "01012345")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAccount_404_notFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Bank account was not found"))
                .when(accountService).deleteAccount(anyString(), anyString());

        mockMvc.perform(delete("/v1/accounts/{accountNumber}", "01999999")
                        .principal(principal("usr-123"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Bank account was not found"));
    }

    @Test
    void updateAccount_200_ok() throws Exception {
        BankAccountResponse updated = BankAccountResponse.builder()
                .accountNumber("01012345")
                .sortCode("10-10-10")
                .name("Updated Name")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.ZERO)
                .currency("GBP")
                .build();

        when(accountService.updateAccount(anyString(), anyString(), Mockito.any()))
                .thenReturn(updated);

        String body = "{\n" +
                "  \"name\": \"Updated Name\",\n" +
                "  \"accountType\": \"personal\"\n" +
                "}";

        mockMvc.perform(patch("/v1/accounts/{accountNumber}", "01012345")
                        .principal(principal("usr-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountNumber").value("01012345"))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.accountType").value("personal"));
    }

    @Test
    void updateAccount_403_forbidden_whenAccessingAnotherUsersAccount() throws Exception {
        when(accountService.updateAccount(anyString(), anyString(), Mockito.any()))
                .thenThrow(new AccessDeniedException("The user is not allowed to access the bank account details"));

        String body = "{\n" +
                "  \"name\": \"Updated Name\"\n" +
                "}";

        mockMvc.perform(patch("/v1/accounts/{accountNumber}", "01012345")
                        .principal(principal("usr-other"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("The user is not allowed to access the bank account details"));
    }

    @Test
    void updateAccount_404_notFound_whenAccountDoesNotExist() throws Exception {
        when(accountService.updateAccount(anyString(), anyString(), Mockito.any()))
                .thenThrow(new ResourceNotFoundException("Bank account was not found"));

        String body = "{\n" +
                "  \"name\": \"Updated Name\"\n" +
                "}";

        mockMvc.perform(patch("/v1/accounts/{accountNumber}", "01999999")
                        .principal(principal("usr-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Bank account was not found"));
    }

}
