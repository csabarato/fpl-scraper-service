package com.csebo.fplscraper.fplscraper.app.mapper;

import com.csebo.fplscraper.fplscraper.app.repository.GameweekEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonDataMapperTest {

    @Test
    void mapJsonToGameweekEntities() {

        List<GameweekEntity> entities = JsonDataMapper.mapJsonToGameweekEntities(
                """
                {
                "events": [
                        {
                            "id": 1,
                            "name": "Gameweek 1",
                            "deadline_time": "2024-10-19T10:00:00Z",
                            "release_time": null
                        }
                        ]
                }""");

        assertEquals(1, entities.get(0).getGameweekNum());
        assertEquals(LocalDateTime.of(2024,10,19,10,0,0), entities.get(0).getDeadlineTime());
    }
}
