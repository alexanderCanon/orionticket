package com.orionticket.identity.infrastructure.adapters.in.rest;

import com.orionticket.identity.application.port.in.LoginUserUseCase;
import com.orionticket.identity.application.port.in.RegisterUserUseCase;
import com.orionticket.identity.domain.model.User;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.RegisterRequest;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.RegisterResponse;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.LoginRequest;
import com.orionticket.identity.infrastructure.adapters.in.rest.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Buyer registration and login endpoints")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;

    @Operation(summary = "Register buyer", description = "Registers a buyer account in UNVERIFIED status.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Buyer registered"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        
        User user = registerUserUseCase.registerBuyer(
                request.getEmail(),
                request.getPassword(),
                request.getFullName(),
                request.getPhone()
        );

        RegisterResponse response = RegisterResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .status(user.getStatus())
                .build();
                
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = loginUserUseCase.login(request.getEmail(), request.getPassword());
        User user = loginUserUseCase.getUserByEmail(request.getEmail());

        LoginResponse response = LoginResponse.builder()
                .accessToken(token)
                .userId(user.getUserId())
                .roleId(user.getRoleId())
                .build();

        return ResponseEntity.ok(response);
    }
}
