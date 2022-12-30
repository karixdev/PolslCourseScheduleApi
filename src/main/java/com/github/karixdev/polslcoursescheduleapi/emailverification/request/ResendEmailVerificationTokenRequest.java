package com.github.karixdev.polslcoursescheduleapi.emailverification.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResendEmailVerificationTokenRequest {
    @JsonProperty("email")
    @Email
    private String email;
}
