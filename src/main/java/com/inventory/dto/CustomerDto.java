package com.inventory.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {

    private Long id;

    @NotBlank(message = "Customer name is required")
    @Size(min = 2, max = 150, message = "Customer name must be between 2 and 150 characters")
    private String name;

    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    private String phone;

    @Email(message = "Email should be a valid format")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;
}
