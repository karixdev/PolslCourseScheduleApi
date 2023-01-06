package com.github.karixdev.polslcoursescheduleapi.discord;

import com.github.karixdev.polslcoursescheduleapi.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscordWebhookRepository extends JpaRepository<DiscordWebhook, Long> {
    @Query("""
            SELECT discordWebhook
            FROM DiscordWebhook discordWebhook
            WHERE discordWebhook.url = :url
            """)
    Optional<DiscordWebhook> findByUrl(@Param("url") String url);

    @Query("""
            SELECT DISTINCT discordWebhook
            FROM DiscordWebhook discordWebhook
            LEFT JOIN FETCH discordWebhook.schedules
            WHERE discordWebhook.addedBy = :user
            """)
    List<DiscordWebhook> findByAddedBy(@Param("user") User user);
}
