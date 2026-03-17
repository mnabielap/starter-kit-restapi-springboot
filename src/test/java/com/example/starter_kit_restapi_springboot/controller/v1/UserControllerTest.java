package com.example.starter_kit_restapi_springboot.controller.v1;

import com.example.starter_kit_restapi_springboot.dto.request.CreateUserRequest;
import com.example.starter_kit_restapi_springboot.dto.request.UpdateUserRequest;
import com.example.starter_kit_restapi_springboot.dto.response.PagedResponse;
import com.example.starter_kit_restapi_springboot.dto.response.UserResponse;
import com.example.starter_kit_restapi_springboot.entity.Role;
import com.example.starter_kit_restapi_springboot.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUserShouldReturnCreatedResponse() {
        CreateUserRequest request = new CreateUserRequest();
        UserResponse response = response();
        when(userService.createUser(request)).thenReturn(response);

        var entity = userController.createUser(request);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(entity.getBody()).isEqualTo(response);
    }

    @Test
    void getUsersShouldMapSnakeCaseSortFieldAndDescendingOrder() {
        PagedResponse<UserResponse> response = new PagedResponse<>(List.of(response()), 2, 5, 6, 2);
        when(userService.getAllUsers(any(Pageable.class), eq("ali"), eq("all"), eq(Role.ADMIN))).thenReturn(response);

        assertThat(userController.getUsers("ali", "all", "admin", 2, 5, "created_at:desc").getStatusCode())
                .isEqualTo(HttpStatus.OK);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).getAllUsers(pageableCaptor.capture(), eq("ali"), eq("all"), eq(Role.ADMIN));
        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getUsersShouldDefaultToAscendingSortAndIgnoreInvalidRole() {
        when(userService.getAllUsers(any(Pageable.class), isNull(), eq("email"), isNull()))
                .thenReturn(new PagedResponse<>(List.of(), 1, 10, 0, 0));

        userController.getUsers(null, "email", "unknown", 1, 10, "updated_at");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userService).getAllUsers(pageableCaptor.capture(), isNull(), eq("email"), isNull());
        assertThat(pageableCaptor.getValue().getSort().getOrderFor("updatedAt").getDirection())
                .isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getUsersShouldTreatBlankRoleAsNoRoleFilter() {
        when(userService.getAllUsers(any(Pageable.class), eq("ali"), eq("all"), isNull()))
                .thenReturn(new PagedResponse<>(List.of(), 1, 10, 0, 0));

        userController.getUsers("ali", "all", "", 1, 10, "id:asc");

        verify(userService).getAllUsers(any(Pageable.class), eq("ali"), eq("all"), isNull());
    }

    @Test
    void getUserShouldReturnOkResponse() {
        UserResponse response = response();
        when(userService.getUserById(1L)).thenReturn(response);

        assertThat(userController.getUser(1L).getBody()).isEqualTo(response);
    }

    @Test
    void updateUserShouldReturnOkResponse() {
        UpdateUserRequest request = new UpdateUserRequest();
        UserResponse response = response();
        when(userService.updateUser(1L, request)).thenReturn(response);

        assertThat(userController.updateUser(1L, request).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteUserShouldReturnNoContent() {
        assertThat(userController.deleteUser(1L).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(userService).deleteUser(1L);
    }

    private UserResponse response() {
        return UserResponse.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .role(Role.ADMIN)
                .build();
    }
}
