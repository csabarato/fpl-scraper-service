package com.csebo.fplscraper.fplscraper.app.controller;

import com.csebo.fplscraper.fplscraper.app.service.PlayerService;
import lombok.extern.slf4j.Slf4j;
import org.SwaggerCodeGenExample.api.DataApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class PlayerController implements DataApi {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public ResponseEntity<Void> updateAll() {
        playerService.saveDataFromFplServer();
        log.info("players saved to the DB");
        return ResponseEntity.ok().build();
    }
}
