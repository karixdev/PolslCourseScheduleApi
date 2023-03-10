package com.github.karixdev.polslcoursescheduleapi.discord.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDiscordWebhookRequest {
    @JsonProperty("schedules_ids")
    @NotEmpty
    private Set<Long> schedulesIds;
}
