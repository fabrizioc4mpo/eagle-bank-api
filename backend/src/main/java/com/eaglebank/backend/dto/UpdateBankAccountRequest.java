package com.eaglebank.backend.dto;

import com.eaglebank.backend.model.AccountType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Partial update request for BankAccount. Only non-null fields will be applied.
 * Balance, currency and sortCode are not updatable via this endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBankAccountRequest {
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    private AccountType accountType;
}
