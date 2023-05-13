package com.example.discordnotificationservice.discord;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordWebhookRepository
        extends MongoRepository<DiscordWebhook, String> {
    @Query("{'discordApiId': :#{#discordApiId}}")
    Optional<DiscordWebhook> findByDiscordApiId(
            @Param("discordApiId") String discordApiId
    );

    @Query("{'token': :#{#token}}")
    Optional<DiscordWebhook> findByToken(
            @Param("token") String token
    );

    @Query("{'addedBy': :#{#addedBy}}")
    Page<DiscordWebhook> findByAddedBy(
            @Param("addedBy") String addedBy,
            Pageable pageable
    );
}
