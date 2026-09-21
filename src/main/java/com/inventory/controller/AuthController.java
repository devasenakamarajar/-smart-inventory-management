package com.inventory.controller;

import com.inventory.dto.AuthResponseDto;
import com.inventory.dto.LoginRequestDto;
import com.inventory.dto.RegisterRequestDto;
import com.inventory.entity.Role;
import com.inventory.entity.User;
import com.inventory.repository.RoleRepository;
import com.inventory.repository.UserRepository;
import com.inventory.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager,
                         UserRepository userRepository,
                         RoleRepository roleRepository,
                         PasswordEncoder passwordEncoder,
                         JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        String role = user.getRoles().stream().findFirst().map(Role::getName).orElse("STAFF");

        AuthResponseDto response = new AuthResponseDto(token, user.getUsername(), role);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()
                || userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "Username or email is already registered");
        }

        String requestedRole = registerRequest.getRole() == null
            ? "STAFF"
                : registerRequest.getRole().trim().toUpperCase(Locale.ROOT);
        if (!requestedRole.equals("ADMIN") && !requestedRole.equals("STAFF")) {
            throw new ResponseStatusException(BAD_REQUEST, "Registration role must be ADMIN or STAFF");
        }

        Role role = roleRepository.findByName(requestedRole)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(requestedRole);
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setUsername(registerRequest.getUsername().trim());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName().trim());
        user.setEmail(registerRequest.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setActive(true);
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("Account created successfully. You can now sign in.");
    }
}
