package com.eaglebank.backend.controller;

import com.eaglebank.backend.dto.AddressDto;
import com.eaglebank.backend.dto.ErrorResponse;
import com.eaglebank.backend.dto.UpdateUserRequest;
import com.eaglebank.backend.dto.UserResponse;
import com.eaglebank.backend.exception.GlobalExceptionHandler;
import com.eaglebank.backend.exception.ResourceNotFoundException;
import com.eaglebank.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private UserService userService;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        userService = Mockito.mock(UserService.class);
        UserController controller = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void updateUser_200_ok_whenUpdatingSelf() throws Exception {
        Mockito.when(userService.updateUser(anyString(), any(UpdateUserRequest.class)))
                .thenReturn(sampleUserResponse());

        String body = "{" +
                "\"name\":\"Jane Doe\"," +
                "\"phoneNumber\":\"+441234567890\"" +
                "}";

        mockMvc.perform(patch("/v1/users/{userId}", "usr-123")
                        .principal(principal("usr-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("usr-123"))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+441234567890"));
    }

    @Test
    void updateUser_403_forbidden_whenUpdatingAnotherUser() throws Exception {
        String body = "{\"name\":\"Hacker\"}";

        mockMvc.perform(patch("/v1/users/{userId}", "usr-999")
                        .principal(principal("usr-123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("You are not allowed to access this resource"));
    }

    @Test
    void updateUser_404_notFound_whenUserDoesNotExist() throws Exception {
        Mockito.when(userService.updateUser(anyString(), any(UpdateUserRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found"));

        String body = "{\"name\":\"Ghost\"}";

        mockMvc.perform(patch("/v1/users/{userId}", "usr-missing")
                        .principal(principal("usr-missing"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    private UserResponse sampleUserResponse() {
        return UserResponse.builder()
                .id("usr-123")
                .name("Jane Doe")
                .email("jane@example.com")
                .phoneNumber("+441234567890")
                .address(AddressDto.builder()
                        .line1("1 Main St")
                        .town("Townsville")
                        .county("Countyshire")
                        .postcode("AB1 2CD")
                        .build())
                .build();
    }

    private Principal principal(String userId) {
        return () -> userId;
    }
}
