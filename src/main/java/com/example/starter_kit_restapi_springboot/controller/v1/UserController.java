package com.example.starter_kit_restapi_springboot.controller.v1;

import com.example.starter_kit_restapi_springboot.dto.request.CreateUserRequest;
import com.example.starter_kit_restapi_springboot.dto.request.UpdateUserRequest;
import com.example.starter_kit_restapi_springboot.dto.response.PagedResponse;
import com.example.starter_kit_restapi_springboot.dto.response.UserResponse;
import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PagedResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "all") String scope,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "id:asc") String sortBy) {
        
        // 1. Handle Sorting
        String[] sortParts = sortBy.split(":");
        String sortField = sortParts[0];
        String sortDirection = sortParts.length > 1 ? sortParts[1] : "asc";
        
        // Map snake_case to camelCase
        if ("created_at".equals(sortField)) sortField = "createdAt";
        if ("updated_at".equals(sortField)) sortField = "updatedAt";

        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sortField));
        
        // 2. Handle Role Conversion (Case-Insensitive)
        Role roleEnum = null;
        if (role != null && !role.isEmpty()) {
            try {
                roleEnum = Role.valueOf(role.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid role string provided; treat as null or handle error.
                // For now, we leave it as null, which means "no role filter"
            }
        }
        
        return ResponseEntity.ok(userService.getAllUsers(pageable, search, scope, roleEnum));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @userSecurity.hasUserId(authentication, #userId)")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @userSecurity.hasUserId(authentication, #userId)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(userId, request));
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or @userSecurity.hasUserId(authentication, #userId)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}