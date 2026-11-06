package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void ciFailureDemo() {
        fail("Intentional CI failure: verifying that GitHub Actions reports failed tests correctly.");
    }

    @Test
    void findByRoleReturnsResponsesForMatchingRole() {
        AppUser student = user(1L, "student", "student@example.edu", Role.STUDENT);
        when(userRepository.findByRole(Role.STUDENT)).thenReturn(List.of(student));

        List<UserResponse> results = userService.findByRole(Role.STUDENT);

        assertEquals(1, results.size());
        assertEquals(student.getId(), results.getFirst().id());
        assertEquals(Role.STUDENT, results.getFirst().role());
    }

    @Test
    void findRejectsUserWithDifferentRole() {
        when(userRepository.findById(7L)).thenReturn(Optional.of(user(7L, "teacher", "teacher@example.edu", Role.TEACHER)));

        assertThrows(ResourceNotFoundException.class, () -> userService.find(7L, Role.STUDENT));
    }

    @Test
    void createNormalizesFieldsAndEncodesPassword() {
        UserRequest request = new UserRequest(" student ", "secret", " Alex Smith ", " ALEX@Example.edu ",
                "Computer Science", "CSE-1", Role.ADMIN);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        UserResponse result = userService.create(request, Role.STUDENT);

        assertEquals(3L, result.id());
        assertEquals("student", result.username());
        assertEquals("Alex Smith", result.fullName());
        assertEquals("alex@example.edu", result.email());
        assertEquals("CSE-1", result.studentId());
        assertEquals(Role.STUDENT, result.role());
        var savedUser = forClass(AppUser.class);
        verify(userRepository).save(savedUser.capture());
        assertEquals("encoded-secret", savedUser.getValue().getPassword());
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void createRejectsStudentWithoutStudentId() {
        UserRequest request = new UserRequest("student", "secret", "Alex Smith", "alex@example.edu",
                "Computer Science", null, null);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.create(request, Role.STUDENT));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void updateChangesProfileAndKeepsPasswordWhenNoNewPasswordIsProvided() {
        AppUser existing = user(4L, "student", "old@example.edu", Role.STUDENT);
        existing.setPassword("existing-hash");
        when(userRepository.findById(4L)).thenReturn(Optional.of(existing));
        when(userRepository.findAll()).thenReturn(List.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        UserRequest request = new UserRequest(null, " ", "Updated Name", "updated@example.edu",
                "Mathematics", "CSE-1", null);

        UserResponse result = userService.update(4L, request, Role.STUDENT);

        assertEquals("Updated Name", result.fullName());
        assertEquals("updated@example.edu", result.email());
        assertEquals("existing-hash", existing.getPassword());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deleteRemovesUserOfRequestedRole() {
        AppUser student = user(5L, "student", "student@example.edu", Role.STUDENT);
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));

        userService.delete(5L, Role.STUDENT);

        verify(userRepository).delete(student);
    }

    private AppUser user(Long id, String username, String email, Role role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("hash");
        user.setFullName("Test User");
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
