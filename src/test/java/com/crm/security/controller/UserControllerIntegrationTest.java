// src/test/java/com/crm/security/controller/UserControllerIntegrationTest.java
package com.crm.security.controller;

import com.crm.security.dto.UserRequestDTO;
import com.crm.security.model.Role;
import com.crm.security.model.User;
import com.crm.security.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clear database
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test@example.com");
        testUser.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));
        testUser.setEnabled(true);
        testUser.setAccountNonLocked(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("POST /api/users - Should create user successfully (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("newuser");
        requestDTO.setPassword("newpass123");
        requestDTO.setEmail("newuser@example.com");
        requestDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_MANAGER)));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_MANAGER")))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.accountNonLocked").value(true))
                .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be in response
    }

    @Test
    @DisplayName("POST /api/users - Should return 400 when username is blank")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenUsernameIsBlank() throws Exception {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("");
        requestDTO.setPassword("password123");
        requestDTO.setEmail("test@example.com");
        requestDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.username").exists());
    }

    @Test
    @DisplayName("POST /api/users - Should return 400 when username is too short")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenUsernameIsTooShort() throws Exception {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("ab");
        requestDTO.setPassword("password123");
        requestDTO.setEmail("test@example.com");
        requestDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.username").exists());
    }

    @Test
    @DisplayName("POST /api/users - Should return 400 when password is too short")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenPasswordIsTooShort() throws Exception {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("validuser");
        requestDTO.setPassword("12345");
        requestDTO.setEmail("test@example.com");
        requestDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    @DisplayName("POST /api/users - Should return 400 when email is invalid")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("validuser");
        requestDTO.setPassword("password123");
        requestDTO.setEmail("invalid-email");
        requestDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    @DisplayName("POST /api/users - Should return 409 when username already exists")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenUsernameExists() throws Exception {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("testuser"); // Already exists
        requestDTO.setPassword("password123");
        requestDTO.setEmail("different@example.com");
        requestDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username already exists: testuser"));
    }

    @Test
    @DisplayName("POST /api/users - Should return 409 when email already exists")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenEmailExists() throws Exception {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("differentuser");
        requestDTO.setPassword("password123");
        requestDTO.setEmail("test@example.com"); // Already exists
        requestDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists: test@example.com"));
    }

    @Test
    @DisplayName("POST /api/users - Should return 403 when user is not ADMIN")
    @WithMockUser(roles = "MANAGER")
    void shouldReturn403WhenNotAdmin() throws Exception {
        // Given
        UserRequestDTO requestDTO = new UserRequestDTO();
        requestDTO.setUsername("newuser");
        requestDTO.setPassword("password123");
        requestDTO.setEmail("newuser@example.com");
        requestDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return user by id (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUserById() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_USER")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return 404 when user not found")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUserNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    @DisplayName("GET /api/users/username/{username} - Should return user by username")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnUserByUsername() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/username/{username}", "testuser"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/users - Should return all users (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnAllUsers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should update user successfully (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        UserRequestDTO updateDTO = new UserRequestDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setPassword("newpassword123");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_MANAGER)));

        // When & Then
        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_MANAGER")));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should return 404 when updating non-existent user")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
        // Given
        UserRequestDTO updateDTO = new UserRequestDTO();
        updateDTO.setUsername("updateduser");
        updateDTO.setPassword("password123");
        updateDTO.setEmail("updated@example.com");
        updateDTO.setRoles(new HashSet<>(Set.of(Role.ROLE_USER)));

        // When & Then
        mockMvc.perform(put("/api/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should delete user successfully (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteUserSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should return 404 when deleting non-existent user")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/enable - Should enable user (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldEnableUser() throws Exception {
        // Given - disable user first
        testUser.setEnabled(false);
        userRepository.save(testUser);

        // When & Then
        mockMvc.perform(patch("/api/users/{id}/enable", testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify user is enabled
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/disable - Should disable user (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldDisableUser() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/users/{id}/disable", testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify user is disabled
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/lock - Should lock user (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldLockUser() throws Exception {
        // When & Then
        mockMvc.perform(patch("/api/users/{id}/lock", testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify user is locked
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNonLocked").value(false));
    }

    @Test
    @DisplayName("PATCH /api/users/{id}/unlock - Should unlock user (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void shouldUnlockUser() throws Exception {
        // Given - lock user first
        testUser.setAccountNonLocked(false);
        userRepository.save(testUser);

        // When & Then
        mockMvc.perform(patch("/api/users/{id}/unlock", testUser.getId()))
                .andDo(print())
                .andExpect(status().isOk());

        // Verify user is unlocked
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNonLocked").value(true));
    }
}