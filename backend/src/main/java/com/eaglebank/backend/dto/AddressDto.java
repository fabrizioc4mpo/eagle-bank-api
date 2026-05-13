package com.eaglebank.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {
    @NotBlank(message = "Line 1 is required")
    private String line1;
    private String line2;
    private String line3;
    @NotBlank(message = "Town is required")
    private String town;
    @NotBlank(message = "County is required")
    private String county;
    @NotBlank(message = "Postcode is required")
    private String postcode;
}
