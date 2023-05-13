package com.example.discordnotificationservice.webhook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookRepository
        extends MongoRepository<Webhook, String> {
    @Query("{'discordApiId': :#{#discordApiId}}")
    Optional<Webhook> findByDiscordApiId(
            @Param("discordApiId") String discordApiId
    );

    @Query("{'discordToken': :#{#discordToken}}")
    Optional<Webhook> findByToken(
            @Param("discordToken") String discordToken
    );

    @Query("{'addedBy': :#{#addedBy}}")
    Page<Webhook> findByAddedBy(
            @Param("addedBy") String addedBy,
            Pageable pageable
    );
}
