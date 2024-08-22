package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.mapper.PlayerMapper;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerEntity;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerRepository;
import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PlayerService {

    PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public void savePlayersFromFplServer() {
        String jsonResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/bootstrap-static/");
        playerRepository.saveAll(PlayerMapper.mapJsonToPlayerEntity(jsonResponse));
    }

    public String getNameById(Integer id){
        Optional<PlayerEntity> playerEntityOptional = playerRepository.findById(id);
        PlayerEntity playerEntity = playerEntityOptional.orElseThrow(() -> new EntityNotFoundException("Entity not found with ID: "+ id));
        return playerEntity.getName();
    }
}
