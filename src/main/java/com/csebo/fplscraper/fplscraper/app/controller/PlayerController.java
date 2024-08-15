package com.csebo.fplscraper.fplscraper.app.controller;

import com.csebo.fplscraper.fplscraper.app.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.SwaggerCodeGenExample.api.PlayerApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class PlayerController implements PlayerApi {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public ResponseEntity<Void> updatePlayers() {
        playerService.savePlayersFromFplServer();
        log.info("players saved to the DB");
        return ResponseEntity.ok().build();
    }
}
