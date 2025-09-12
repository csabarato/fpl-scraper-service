package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.mapper.JsonDataMapper;
import com.csebo.fplscraper.fplscraper.app.repository.GameweekRepository;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerEntity;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerRepository;
import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final GameweekRepository gameweekRepository;

    public PlayerService(PlayerRepository playerRepository, GameweekRepository gameweekRepository) {
        this.playerRepository = playerRepository;
        this.gameweekRepository = gameweekRepository;
    }

    public void saveDataFromFplServer() {
        String jsonResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/bootstrap-static/");

        playerRepository.saveAll(JsonDataMapper.mapJsonToPlayerEntities(jsonResponse));
        gameweekRepository.saveAll(JsonDataMapper.mapJsonToGameweekEntities(jsonResponse));
    }

    public PlayerEntity getById(Integer id){
        Optional<PlayerEntity> playerEntityOptional = playerRepository.findById(id);
        return playerEntityOptional.orElseGet(() -> fetchMissingPlayerDataFromFplServer(id));
    }

    private PlayerEntity fetchMissingPlayerDataFromFplServer(Integer id) {

        String jsonResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/bootstrap-static/");

        PlayerEntity playerEntity = JsonDataMapper.findPlayerByIdInJson(jsonResponse, id)
                .orElseThrow(()
                        -> new EntityNotFoundException("Player data not found after fetching from FPL server for ID: " + id));

        return playerRepository.save(playerEntity);
    }
}
