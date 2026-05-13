package com.eaglebank.backend.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BankAccountBeanValidationTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void accountNumber_mustMatchPattern() {
        BankAccount a = BankAccount.builder()
                .accountNumber("ABC123")
                .sortCode("10-10-10")
                .name("Personal")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.ZERO)
                .currency("GBP")
                // user is required by JPA but not by bean validation here
                .build();

        var violations = validator.validateProperty(a, "accountNumber");
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("^01");
    }

    @Test
    void validAccountNumber_passes() {
        BankAccount a = BankAccount.builder()
                .accountNumber("01012345")
                .sortCode("10-10-10")
                .name("Personal")
                .accountType(AccountType.PERSONAL)
                .balance(BigDecimal.ZERO)
                .currency("GBP")
                .build();

        var violations = validator.validateProperty(a, "accountNumber");
        assertThat(violations).isEmpty();
    }
}
