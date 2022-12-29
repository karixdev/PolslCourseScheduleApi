package com.github.karixdev.polslcoursescheduleapi.auth.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @JsonProperty("email")
    @Email
    private String email;

    @JsonProperty("password")
    @Size(min = 8, max = 255)
    private String password;
}