package com.github.karixdev.polslcoursescheduleapi.schedule;

import com.github.karixdev.polslcoursescheduleapi.schedule.payload.request.ScheduleRequest;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.security.CurrentUser;
import com.github.karixdev.polslcoursescheduleapi.security.UserPrincipal;
import com.github.karixdev.polslcoursescheduleapi.shared.payload.response.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService service;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ScheduleResponse> add(
            @Valid @RequestBody ScheduleRequest payload,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        return new ResponseEntity<>(
                service.add(payload, userPrincipal),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<SuccessResponse> delete(
            @PathVariable(name = "id") Long id
    ) {
        return new ResponseEntity<>(
                service.delete(id),
                HttpStatus.OK
        );
    }
}