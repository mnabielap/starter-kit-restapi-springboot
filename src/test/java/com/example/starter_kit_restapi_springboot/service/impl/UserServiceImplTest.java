package com.example.starter_kit_restapi_springboot.service.impl;

import com.example.starter_kit_restapi_springboot.dto.request.CreateUserRequest;
import com.example.starter_kit_restapi_springboot.dto.request.UpdateUserRequest;
import com.example.starter_kit_restapi_springboot.dto.response.PagedResponse;
import com.example.starter_kit_restapi_springboot.dto.response.UserResponse;
import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.entity.User;
import com.example.starter_kit_restapi_springboot.exception.DuplicateResourceException;
import com.example.starter_kit_restapi_springboot.exception.ResourceNotFoundException;
import com.example.starter_kit_restapi_springboot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllUsersShouldReturnMappedPagedResponse() {
        Pageable pageable = PageRequest.of(0, 2);
        User user = user(1L, "Alice", "alice@example.com", Role.ADMIN);

        when(userRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        PagedResponse<UserResponse> response = userService.getAllUsers(pageable, "ali", "all", Role.ADMIN);

        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getLimit()).isEqualTo(2);
        assertThat(response.getTotalResults()).isEqualTo(1);
        assertThat(response.getResults()).singleElement().satisfies(result -> {
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("alice@example.com");
        });
    }

    @Test
    void getUserByIdShouldReturnMappedUser() {
        User user = user(2L, "Alice", "alice@example.com", Role.USER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(2L);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void getUserByIdShouldFailWhenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 99");
    }

    @Test
    void createUserShouldPersistEncodedPassword() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Alice");
        request.setEmail("alice@example.com");
        request.setPassword("password123");
        request.setRole(Role.ADMIN);

        User savedUser = user(3L, "Alice", "alice@example.com", Role.ADMIN);

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isEqualTo(3L);
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("alice@example.com");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already taken");
    }

    @Test
    void updateUserShouldChangeNameAndEmailWhenValid() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated");
        request.setEmail("updated@example.com");
        User user = user(4L, "Alice", "alice@example.com", Role.USER);

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateUser(4L, request);

        assertThat(response.getName()).isEqualTo("Updated");
        assertThat(response.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void updateUserShouldKeepSameEmailWithoutCheckingDuplicates() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("alice@example.com");
        User user = user(5L, "Alice", "alice@example.com", Role.USER);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        userService.updateUser(5L, request);

        verify(userRepository, never()).existsByEmail("alice@example.com");
    }

    @Test
    void updateUserShouldRejectDuplicateTargetEmail() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("taken@example.com");
        User user = user(6L, "Alice", "alice@example.com", Role.USER);

        when(userRepository.findById(6L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(6L, request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already taken");
    }

    @Test
    void updateUserShouldLeaveUserUnchangedWhenRequestHasNoUpdates() {
        UpdateUserRequest request = new UpdateUserRequest();
        User user = user(61L, "Alice", "alice@example.com", Role.USER);

        when(userRepository.findById(61L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateUser(61L, request);

        assertThat(response.getName()).isEqualTo("Alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void updateUserShouldFailWhenUserDoesNotExist() {
        when(userRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(7L, new UpdateUserRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 7");
    }

    @Test
    void deleteUserShouldDeleteExistingUser() {
        when(userRepository.existsById(8L)).thenReturn(true);

        userService.deleteUser(8L);

        verify(userRepository).deleteById(8L);
    }

    @Test
    void deleteUserShouldFailWhenUserDoesNotExist() {
        when(userRepository.existsById(9L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(9L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: 9");
    }

    private User user(Long id, String name, String email, Role role) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .password("encoded-password")
                .role(role)
                .build();
    }
}
