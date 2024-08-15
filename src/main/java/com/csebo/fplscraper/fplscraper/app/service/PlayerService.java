package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.mapper.PlayerMapper;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerRepository;
import com.csebo.fplscraper.fplscraper.app.scraper.PlayerScraper;
import org.springframework.stereotype.Service;

@Service
public class PlayerService {

    PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void savePlayersFromFplServer() {
        String jsonResponse = PlayerScraper.scrapePlayersFromFplServer();
        playerRepository.saveAll(PlayerMapper.mapJsonToPlayerEntity(jsonResponse));
    }
}
