package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private boolean active;
    private LocalDateTime registeredAt;
}