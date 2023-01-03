package com.github.karixdev.polslcoursescheduleapi.discord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordWebhookRepository extends JpaRepository<DiscordWebhook, Long> {
    @Query("""
            SELECT discordWebhook
            FROM DiscordWebhook discordWebhook
            WHERE discordWebhook.url = :url
            """)
    Optional<DiscordWebhook> findByUrl(@Param("url") String url);
}
