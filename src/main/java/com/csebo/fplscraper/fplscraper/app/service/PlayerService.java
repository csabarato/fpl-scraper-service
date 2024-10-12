package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.mapper.DataMapper;
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

        playerRepository.saveAll(DataMapper.mapJsonToPlayerEntity(jsonResponse));
        gameweekRepository.saveAll(DataMapper.mapJsonToGameweekEntity(jsonResponse));
    }

    public String getNameById(Integer id){
        Optional<PlayerEntity> playerEntityOptional = playerRepository.findById(id);
        PlayerEntity playerEntity = playerEntityOptional.orElseThrow(() -> new EntityNotFoundException("Entity not found with ID: "+ id));
        return playerEntity.getName();
    }
}
