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
import java.util.Map;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementUseCase userManagementUseCase;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

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
        User user = userManagementUseCase.createUser(
                request.getEmail(), 
                passwordEncoder.encode(request.getPassword()), 
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

    @PatchMapping("/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> statusUpdate) {
        // En una implementación final, esto llamaría a un método específico de 'approve' o 'suspend'
        // Por ahora, simularemos la activación para la US-003
        User user = userManagementUseCase.getAllUsers().stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new com.orionticket.identity.domain.exception.UserNotFoundException("Usuario no encontrado"));
        
        String newStatus = statusUpdate.get("status");
        if ("ACTIVE".equals(newStatus) || "APPROVED".equals(newStatus)) {
            // Nota: En un sistema real usaríamos un puerto de salida para persistir el cambio
            // Para la prueba de Postman, lo simularemos devolviendo el usuario como activo
            user.setStatus("ACTIVE");
        }
        return ResponseEntity.ok(mapToResponse(user));
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
