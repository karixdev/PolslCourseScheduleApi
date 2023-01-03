package com.github.karixdev.polslcoursescheduleapi.discord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiscordWebHookRepository extends JpaRepository<DiscordWebHook, Long> {
    @Query("""
            SELECT discordWebHook
            FROM DiscordWebHook discordWebHook
            WHERE discordWebHook.url = :url
            """)
    Optional<DiscordWebHook> findByUrl(@Param("url") String url);
}
