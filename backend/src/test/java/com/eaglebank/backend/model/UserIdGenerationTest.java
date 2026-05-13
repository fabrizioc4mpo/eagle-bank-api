package com.eaglebank.backend.model;

import com.eaglebank.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UserIdGenerationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void shouldGenerateUserIdWithCorrectPrefixAndFormat() {
        // Use a unique email per test execution to avoid unique-constraint conflicts
        String uniqueEmail = "test-" + java.util.UUID.randomUUID() + "@example.com";
        User user = User.builder()
                .name("Test User")
                .email(uniqueEmail)
                .phoneNumber("+441234567890")
                .password("password")
                .address(Address.builder()
                        .line1("123 Test St")
                        .town("Test Town")
                        .county("Test County")
                        .postcode("TE1 1ST")
                        .build())
                .build();

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getId()).startsWith("usr-");
        assertThat(savedUser.getId()).matches("^usr-[A-Za-z0-9]+$");
    }
}
