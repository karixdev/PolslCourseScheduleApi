package com.github.karixdev.webhookservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.UUID;

@Builder
public record WebhookRequest(
		@JsonProperty("discordWebhookUrl")
		@NotBlank
		@NotNull
		String discordWebhookUrl,
		@JsonProperty("addedBy")
		@Nullable
		String addedBy,
		@JsonProperty("schedulesIds")
		@NotNull
		@NotEmpty
		Set<UUID> schedulesIds
) {
}
