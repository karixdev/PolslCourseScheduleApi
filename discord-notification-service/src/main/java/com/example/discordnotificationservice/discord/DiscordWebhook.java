package com.example.discordnotificationservice.discord;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Document
public class DiscordWebhook {
    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Indexed(unique = true)
    private String discordApiId;
    @Indexed(unique = true)
    private String token;

    private String addedBy;

    @Builder.Default
    private Set<UUID> schedules = new LinkedHashSet<>();
}
