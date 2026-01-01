package com.example.starter_kit_restapi_springboot.service;

import com.example.starter_kit_restapi_springboot.dto.request.CreateUserRequest;
import com.example.starter_kit_restapi_springboot.dto.request.UpdateUserRequest;
import com.example.starter_kit_restapi_springboot.dto.response.PagedResponse;
import com.example.starter_kit_restapi_springboot.dto.response.UserResponse;
import com.example.starter_kit_restapi_springboot.entity.Role;
import org.springframework.data.domain.Pageable;

public interface UserService {
    PagedResponse<UserResponse> getAllUsers(Pageable pageable, String search, String scope, Role role);
    
    UserResponse getUserById(Long userId);
    UserResponse createUser(CreateUserRequest request);
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    void deleteUser(Long userId);
}