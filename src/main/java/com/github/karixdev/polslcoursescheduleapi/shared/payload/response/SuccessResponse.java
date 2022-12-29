package com.github.karixdev.polslcoursescheduleapi.shared.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SuccessResponse {
    private final String message = "success";
}
