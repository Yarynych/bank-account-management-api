package ua.yarynych.apiaccountmanagement.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.AuthResponse;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.LoginRequest;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.LogoutRequest;
import ua.yarynych.apiaccountmanagement.entity.dto.auth.RegistrationRequest;
import ua.yarynych.apiaccountmanagement.service.auth.AuthService;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Auth logic for users")
public class AuthenticationController {

    private final AuthService authService;

    @Operation(summary = "Login of users", description = "Provide authentication of user and return jwt tokens")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)) })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registration of users", description = "Provide registration of user and sent confirmation letter. The user must have the admin role for this endpoint")
    @ApiResponse(responseCode = "200", content = {@Content(mediaType = "text/plain", schema = @Schema(type = "string", description = "User registered successfully"))})
    @PostMapping("/registration")
    public ResponseEntity<?> registration(@RequestBody RegistrationRequest request) {
        return ResponseEntity.ok(authService.registration(request));
    }

    @Operation(summary = "Refresh token", description = "Provides token update for the user via refresh token.")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)) })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    @Operation(summary = "Logout", description = "Ensures exit from the account for the user. The logic is more to process the request and return the response without the tokens so that they are not stored on the front-end.")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class)) })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.logout(token));
    }
}
