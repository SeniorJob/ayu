package com.seniorjob;

import com.seniorjob.seniorjobserver.controller.UserController;
import com.seniorjob.seniorjobserver.repository.UserRepository;
import com.seniorjob.seniorjobserver.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
public class UserSessionUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UserRepository userRepository;

    // Initialize MockMvc with Security
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser
    public void testLoginAndSession() throws Exception {
        // Perform login
        mockMvc.perform(post("/api/users/login")
                        .contentType("application/json")
                        .content("{\"phoneNumber\":\"01011111111\",\"password\":\"aaaaaa1\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID")); // Check for JSESSIONID cookie

        // Now perform another request to check if session is maintained
        mockMvc.perform(post("/api/users/detail")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID")); // This should pass if session is maintained
    }
}
