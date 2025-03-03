package ua.yarynych.apiaccountmanagement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.yarynych.apiaccountmanagement.entity.User;
import ua.yarynych.apiaccountmanagement.entity.dto.user.UserUpdateRequest;
import ua.yarynych.apiaccountmanagement.service.UserService;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "User logic", description = "Logic for working with internal users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get users by email", description = "Extracts the users record from the database by email")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) })
    @GetMapping("/get/email")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.findUserByEmail(email, token));
    }

    @Operation(summary = "Get users by id", description = "Extracts the users record from the database by id")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) })
    @GetMapping("/get/id")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> getUserById(@RequestParam Long id, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.findUserById(id, token));
    }

    @Operation(summary = "Get all users", description = "Extracts all users record from the database")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = User.class)))})
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> getAllUsers(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.getAllUsers(token));
    }

    @Operation(summary = "Update user", description = "Update all user fields except email and password")
    @ApiResponse(responseCode = "200", content = { @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)) })
    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateRequest updateRequest, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.updateUser(updateRequest, token));
    }

    @Operation(summary = "Delete user", description = "Delete users by id. In response to the request, it send string about successful deleting")
    @ApiResponse(responseCode = "200", content = {@Content(mediaType = "text/plain", schema = @Schema(type = "string", description = "User deleted successfully"))})
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INTERNAL_USER')")
    public ResponseEntity<?> deleteUser(@RequestParam Long userId, @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(userService.deleteUserById(userId, token));
    }
}
