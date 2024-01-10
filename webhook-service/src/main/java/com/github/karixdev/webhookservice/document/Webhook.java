package com.github.karixdev.webhookservice.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Document(collection = "webhook")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook {

	@Id
	private String id;

	private String addedBy;

	@Builder.Default
	private Set<UUID> schedulesIds = new LinkedHashSet<>();

	private String discordWebhookUrl;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Webhook webhook = (Webhook) o;
		return id != null && Objects.equals(getId(), webhook.getId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getId());
	}

}
