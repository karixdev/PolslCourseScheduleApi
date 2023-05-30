package com.github.karixdev.discordnotificationservice.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DiscordWebhook {
    String discordId;
    String token;
}
