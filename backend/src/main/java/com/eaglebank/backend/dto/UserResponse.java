package com.eaglebank.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String name;
    private AddressDto address;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdTimestamp;
    private LocalDateTime updatedTimestamp;
}
