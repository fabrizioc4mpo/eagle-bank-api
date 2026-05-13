package com.eaglebank.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL;

    @JsonValue
    public String toJson() {
        return this == DEPOSIT ? "deposit" : "withdrawal";
    }

    @JsonCreator
    public static TransactionType fromJson(String value) {
        if (value == null) return null;
        if ("deposit".equalsIgnoreCase(value)) return DEPOSIT;
        if ("withdrawal".equalsIgnoreCase(value)) return WITHDRAWAL;
        throw new IllegalArgumentException("Unsupported transaction type: " + value);
    }
}
