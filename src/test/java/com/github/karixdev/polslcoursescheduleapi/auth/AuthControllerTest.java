package com.github.karixdev.polslcoursescheduleapi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.karixdev.polslcoursescheduleapi.auth.payload.request.RegisterRequest;
import com.github.karixdev.polslcoursescheduleapi.user.exception.EmailNotAvailableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {AuthController.class},
        excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
public class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthService authService;

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void GivenInvalidCredentials_WhenRegister_ThenRespondsWithBadRequestStatus() throws Exception {
        RegisterRequest payload =
                new RegisterRequest("abc", "abc");
        String content = mapper.writeValueAsString(payload);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GivenTakenEmail_WhenRegister_ThenRespondsWithConflictStatus() throws Exception {
        RegisterRequest payload =
                new RegisterRequest("taken@email.com", "password");
        String content = mapper.writeValueAsString(payload);

        doThrow(new EmailNotAvailableException())
                .when(authService)
                .register(eq(payload));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isConflict());
    }
}
