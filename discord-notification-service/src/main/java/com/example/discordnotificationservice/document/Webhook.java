package com.example.discordnotificationservice.document;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@Document
public class Webhook {
    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    private DiscordWebhook discordWebhook;

    private String addedBy;

    @Builder.Default
    private Set<UUID> schedules = new LinkedHashSet<>();
}
