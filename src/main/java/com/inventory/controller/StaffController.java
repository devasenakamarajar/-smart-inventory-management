package com.inventory.controller;

import com.inventory.dto.StaffDto;
import com.inventory.entity.User;
import com.inventory.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final UserRepository userRepository;

    public StaffController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<StaffDto>> getStaff() {
        List<StaffDto> staff = userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream().anyMatch(role -> "STAFF".equalsIgnoreCase(role.getName()) || "USER".equalsIgnoreCase(role.getName())))
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(staff);
    }

    private StaffDto toDto(User user) {
        return new StaffDto(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
                user.getPhone(), user.isActive(), user.getCreatedAt());
    }
}