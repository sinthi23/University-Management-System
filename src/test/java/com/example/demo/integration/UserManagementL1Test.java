package com.example.demo.integration;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:university-integration;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Tag("Layer1")
@Transactional
class UserManagementL1Test {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCreatesStudentAndPersistsEncodedCredentials() throws Exception {
        String request = """
                {
                  "username": "integration-student",
                  "password": "student-secret",
                  "fullName": "Integration Student",
                  "email": "integration.student@example.edu",
                  "department": "Computer Science",
                  "studentId": "INT-1001"
                }
                """;

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.startsWith("/api/students/")))
                .andExpect(jsonPath("$.username").value("integration-student"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());

        AppUser saved = userRepository.findByUsername("integration-student").orElseThrow();
        assertTrue(passwordEncoder.matches("student-secret", saved.getPassword()));
        assertEquals(Role.STUDENT, saved.getRole());
        assertEquals("INT-1001", saved.getStudentId());
    }

    @Test
    void teacherCanReadStudentsFromTheDatabase() throws Exception {
        mockMvc.perform(get("/api/students")
                        .with(user("teacher").roles("TEACHER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username == 'student')].fullName").value(Matchers.contains("Alex Johnson")))
                .andExpect(jsonPath("$[?(@.username == 'student')].role").value(Matchers.contains("STUDENT")));
    }

    @Test
    void duplicateEmailIsRejectedWithoutPersistingTheNewUser() throws Exception {
        String request = """
                {
                  "username": "duplicate-email-student",
                  "password": "student-secret",
                  "fullName": "Duplicate Email Student",
                  "email": "student@university.edu",
                  "department": "Computer Science",
                  "studentId": "INT-1002"
                }
                """;

        mockMvc.perform(post("/api/students")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());

        assertTrue(userRepository.findByUsername("duplicate-email-student").isEmpty());
        assertEquals(1, userRepository.findByRole(Role.STUDENT).size());
    }
}
