package com.github.karixdev.discordnotificationservice.repository;

import com.github.karixdev.discordnotificationservice.document.DiscordWebhook;
import com.github.karixdev.discordnotificationservice.document.Webhook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WebhookRepository
        extends MongoRepository<Webhook, String> {
    @Query("{'addedBy': :#{#addedBy}}")
    Page<Webhook> findByAddedBy(
            @Param("addedBy") String addedBy,
            Pageable pageable
    );

    @Query("{'discordWebhook': :#{#discordWebhook}}")
    Optional<Webhook> findByDiscordWebhook(
            @Param("discordWebhook") DiscordWebhook discordWebhook
    );

    @Query("{'schedules':  { '$in': [:#{#schedule}] }}")
    List<Webhook> findBySchedulesContaining(@Param("schedule") UUID schedule);
}
