package com.eaglebank.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AccountType {
    PERSONAL;

    @JsonValue
    public String toJson() {
        return "personal";
    }

    @JsonCreator
    public static AccountType fromJson(String value) {
        if (value == null) return null;
        if ("personal".equalsIgnoreCase(value)) return PERSONAL;
        throw new IllegalArgumentException("Unsupported account type: " + value);
    }
}
