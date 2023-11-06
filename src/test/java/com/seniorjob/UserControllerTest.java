package com.seniorjob;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seniorjob.seniorjobserver.controller.UserController;
import com.seniorjob.seniorjobserver.domain.entity.UserEntity;
import com.seniorjob.seniorjobserver.service.UserService;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
// ... 다른 필요한 임포트 ...

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void whenLogin_thenReturnJSessionId() throws Exception {
        // given
        String phoneNumber = "01012345678";
        String password = "password";
        LoginRequest loginRequest = new LoginRequest(phoneNumber, password);

        // mocking the authentication process with a UserEntity built via Lombok Builder
        UserEntity mockUser = UserEntity.builder()
                .uid(1L)
                .name("Test User")
                .phoneNumber(phoneNumber)
                .encryptionCode("encryptedPassword")
                .job("Developer")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                // ... set other properties as needed ...
                .build();

        when(userService.authenticate(anyString(), anyString())).thenReturn(mockUser);

        // when & then
        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID")); // Check if the cookie is set
    }
}
