package com.github.karixdev.polslcoursescheduleapi.discord.payload.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.karixdev.polslcoursescheduleapi.discord.DiscordWebHook;
import com.github.karixdev.polslcoursescheduleapi.schedule.payload.response.ScheduleResponse;
import com.github.karixdev.polslcoursescheduleapi.user.repsonse.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscordWebHookResponse {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("url")
    private String url;

    @JsonProperty("schedules")
    private Set<ScheduleResponse> schedules;

    @JsonProperty("added_by")
    @JsonIgnoreProperties({"is_enabled", "user_role"})
    private UserResponse addedBy;

    public DiscordWebHookResponse(DiscordWebHook discordWebHook) {
        this.id = discordWebHook.getId();
        this.url = discordWebHook.getUrl();
        this.addedBy = new UserResponse(discordWebHook.getAddedBy());
        this.schedules = discordWebHook.getSchedules().stream()
                .map(ScheduleResponse::new)
                .collect(Collectors.toSet());
    }
}
