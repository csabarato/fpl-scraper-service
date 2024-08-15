package com.csebo.fplscraper.fplscraper.app.controller;

import org.SwaggerCodeGenExample.api.PlayerApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class PlayerController implements PlayerApi {

    @Override
    public ResponseEntity<Void> updatePlayers() {
        return ResponseEntity.ok().build();
    }
}
