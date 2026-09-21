package com.inventory.controller;

import com.inventory.dto.RegisterRequestDto;
import com.inventory.entity.Role;
import com.inventory.repository.RoleRepository;
import com.inventory.repository.UserRepository;
import com.inventory.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil = new JwtUtil();

    @Test
    void rejectsRolesOutsideAdminAndStaff() {
        AuthController controller = controller();
        RegisterRequestDto request = request("MANAGER");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.register(request));

        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void defaultsRegistrationToStaff() {
        AuthController controller = controller();
        RegisterRequestDto request = request(null);
        Role staff = new Role();
        staff.setName("STAFF");
        when(userRepository.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(roleRepository.findByName("STAFF")).thenReturn(Optional.of(staff));
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");

        controller.register(request);

        verify(roleRepository).findByName("STAFF");
        verify(userRepository).save(any());
    }

    private AuthController controller() {
        return new AuthController(authenticationManager, userRepository, roleRepository, passwordEncoder, jwtUtil);
    }

    private RegisterRequestDto request(String role) {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setUsername("staff-user");
        request.setPassword("secret123");
        request.setFullName("Staff User");
        request.setEmail("staff@example.com");
        request.setRole(role);
        return request;
    }
}
