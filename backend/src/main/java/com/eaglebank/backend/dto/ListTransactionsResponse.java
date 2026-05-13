package com.eaglebank.backend.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListTransactionsResponse {
    private List<TransactionResponse> transactions;
}
