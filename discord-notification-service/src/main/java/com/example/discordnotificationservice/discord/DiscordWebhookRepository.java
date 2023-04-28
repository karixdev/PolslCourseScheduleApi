package com.example.discordnotificationservice.discord;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscordWebhookRepository
        extends MongoRepository<DiscordWebhook, String> {}
