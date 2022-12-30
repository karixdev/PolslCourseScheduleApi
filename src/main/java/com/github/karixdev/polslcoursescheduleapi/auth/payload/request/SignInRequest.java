package com.github.karixdev.polslcoursescheduleapi.auth.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignInRequest {
    @JsonProperty("email")
    @Email
    private String email;

    @JsonProperty("password")
    @Size(min = 8, max = 255)
    private String password;
}
