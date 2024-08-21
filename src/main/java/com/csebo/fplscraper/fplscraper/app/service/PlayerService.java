package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.mapper.PlayerMapper;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerRepository;
import com.csebo.fplscraper.fplscraper.app.scraper.DataScraper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class PlayerService {

    PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void savePlayersFromFplServer() {
        String jsonResponse = DataScraper.executeGetRequest("https://fantasy.premierleague.com/api/bootstrap-static/");
        playerRepository.saveAll(PlayerMapper.mapJsonToPlayerEntity(jsonResponse));
    }
}
