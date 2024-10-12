package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.repository.GameweekRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class GameweekService {

    private final GameweekRepository gameweekRepository;

    public GameweekService(GameweekRepository gameweekRepository) {
        this.gameweekRepository = gameweekRepository;
    }

    public int getCurrentGameweek() {
        return gameweekRepository.findFirstByDeadlineTimeBeforeOrderByDeadlineTimeDesc(LocalDateTime.now(ZoneOffset.UTC)).getGameweekNum();
    }
}
