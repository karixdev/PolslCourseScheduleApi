package com.github.karixdev.webhookservice.repository;

import com.github.karixdev.webhookservice.document.Webhook;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookRepository extends MongoRepository<Webhook, String> {

	@Query("""
			{
				"discordWebhookUrl": :#{#url}
			}
			""")
	Optional<Webhook> findByDiscordWebhookUrl(@Param("url") String url);

}
