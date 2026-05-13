package com.eaglebank.backend.dto;

import com.eaglebank.backend.model.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBankAccountRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType accountType;
}
