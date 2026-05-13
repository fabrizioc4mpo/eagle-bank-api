package com.eaglebank.backend.dto;

import com.eaglebank.backend.model.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    private String id;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private String reference;
    private String userId;
    private BigDecimal balance;
    private LocalDateTime createdTimestamp;
}
