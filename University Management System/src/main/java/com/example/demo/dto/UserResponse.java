package com.example.demo.dto;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String department,
        String studentId,
        Role role
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(),
                user.getEmail(), user.getDepartment(), user.getStudentId(), user.getRole());
    }
}
