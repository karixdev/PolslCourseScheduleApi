package com.github.karixdev.webhookservice.document;

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
