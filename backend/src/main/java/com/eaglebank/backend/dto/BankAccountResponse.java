package com.eaglebank.backend.dto;

import com.eaglebank.backend.model.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountResponse {
    private String accountNumber;
    private String sortCode;
    private String name;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
}
