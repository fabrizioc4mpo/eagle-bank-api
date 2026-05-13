package com.eaglebank.backend.dto;

import com.eaglebank.backend.model.TransactionType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTransactionRequest {

    @NotNull
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "10000.00")
    @Digits(integer = 5, fraction = 2)
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "GBP", message = "currency must be GBP")
    private String currency;

    @NotNull
    private TransactionType type;

    private String reference;
}
