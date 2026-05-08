package com.orionticket.identity.infrastructure.adapters.in.rest;

import com.orionticket.identity.application.port.in.UserManagementUseCase;
import com.orionticket.identity.domain.model.User;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.UpdateRoleRequest;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementUseCase userManagementUseCase;

    // En producción esto se obtiene del token JWT (Spring Security Principal)
    private final UUID TEMPORARY_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @PutMapping("/{userId}/suspend")
    public ResponseEntity<UserResponse> suspendUser(@PathVariable UUID userId) {
        User updatedUser = userManagementUseCase.suspendUser(userId, TEMPORARY_ADMIN_ID);
        return ResponseEntity.ok(mapToResponse(updatedUser));
    }

    @PutMapping("/{userId}/roles")
    public ResponseEntity<UserResponse> updateUserRole(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        User updatedUser = userManagementUseCase.updateUserRole(userId, request.getNewRoleId(), TEMPORARY_ADMIN_ID);
        return ResponseEntity.ok(mapToResponse(updatedUser));
    }

    @GetMapping
    public ResponseEntity<java.util.List<UserResponse>> getAllUsers() {
        java.util.List<UserResponse> users = userManagementUseCase.getAllUsers().stream()
                .map(this::mapToResponse)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody com.orionticket.identity.infrastructure.adapters.in.rest.dto.CreateUserRequest request) {
        // En un entorno real, la contraseña vendría o se autogeneraría y encriptaría
        // Para este MVP, asumimos que viene en texto plano y la debemos hashear (simulado aquí)
        // Spring Security PasswordEncoder debería usarse aquí idealmente.
        User user = userManagementUseCase.createUser(
                request.getEmail(), 
                "HASHED_" + request.getPassword(), // TODO: inject PasswordEncoder
                request.getFullName(), 
                request.getPhone(), 
                request.getRoleId(), 
                request.getOrganizerId(), 
                TEMPORARY_ADMIN_ID);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(mapToResponse(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody com.orionticket.identity.infrastructure.adapters.in.rest.dto.UpdateUserRequest request) {
        User updatedUser = userManagementUseCase.updateUser(userId, request.getFullName(), request.getPhone(), TEMPORARY_ADMIN_ID);
        return ResponseEntity.ok(mapToResponse(updatedUser));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .status(user.getStatus())
                .roleId(user.getRoleId())
                .organizerId(user.getOrganizerId())
                .build();
    }
}
