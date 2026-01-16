package com.crm.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without authentication")
    void shouldReturn401WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER role should have read access to customers")
    void userRoleShouldHaveReadAccess() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER role should NOT have write access to customers")
    void userRoleShouldNotHaveWriteAccess() throws Exception {
        mockMvc.perform(post("/api/customers"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("MANAGER role should have read and write access")
    void managerRoleShouldHaveWriteAccess() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("MANAGER role should NOT have delete access")
    void managerRoleShouldNotHaveDeleteAccess() throws Exception {
        mockMvc.perform(delete("/api/customers/1"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN role should have full access")
    void adminRoleShouldHaveFullAccess() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("USER role should NOT have access to user management")
    void userRoleShouldNotAccessUserManagement() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}