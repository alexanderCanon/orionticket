package com.orionticket.identity.infrastructure.adapters.in.rest;

import com.orionticket.identity.application.port.in.UserManagementUseCase;
import com.orionticket.identity.domain.model.User;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.CreateStaffRequest;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/organizers")
@RequiredArgsConstructor
@Tag(name = "Organizer Staff", description = "Organizer staff creation endpoints")
public class OrganizerStaffController {

    private final UserManagementUseCase userManagementUseCase;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Create organizer staff", description = "Creates a staff user scoped to an organizer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Staff user created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/{organizerId}/staff")
    public ResponseEntity<UserResponse> createStaff(
            @PathVariable UUID organizerId,
            @Valid @RequestBody CreateStaffRequest request) {
        
        // En un flujo real, el ID del creador vendría del JWT SecurityContext
        UUID temporaryCreatorId = UUID.fromString("00000000-0000-0000-0000-000000000002"); // Simulado como Organizador

        User staff = userManagementUseCase.createOrganizerStaff(
                organizerId,
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getPhone(),
                request.getRoleId(),
                temporaryCreatorId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(staff));
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
