package com.orionticket.identity.infrastructure.adapters.in.rest;

import com.orionticket.identity.application.port.in.RoleManagementUseCase;
import com.orionticket.identity.domain.model.Role;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.CreateRoleRequest;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.RoleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/roles")
@RequiredArgsConstructor
public class RoleManagementController {

    private final RoleManagementUseCase roleManagementUseCase;
    
    // En producción esto se obtiene del token JWT (Spring Security Principal)
    private final UUID TEMPORARY_ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = roleManagementUseCase.createRole(request.getName(), request.getPermissions(), TEMPORARY_ADMIN_ID);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(role));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleManagementUseCase.getAllRoles().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(roles);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable UUID roleId,
            @Valid @RequestBody CreateRoleRequest request) {
        Role role = roleManagementUseCase.updateRole(roleId, request.getName(), request.getPermissions(), TEMPORARY_ADMIN_ID);
        return ResponseEntity.ok(mapToResponse(role));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID roleId) {
        roleManagementUseCase.deleteRole(roleId, TEMPORARY_ADMIN_ID);
        return ResponseEntity.noContent().build();
    }

    private RoleResponse mapToResponse(Role role) {
        return RoleResponse.builder()
                .roleId(role.getRoleId())
                .name(role.getName())
                .permissions(role.getPermissions())
                .build();
    }
}
