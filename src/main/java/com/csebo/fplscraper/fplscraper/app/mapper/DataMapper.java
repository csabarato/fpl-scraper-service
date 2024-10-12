package com.csebo.fplscraper.fplscraper.app.mapper;

import com.csebo.fplscraper.fplscraper.app.repository.GameweekEntity;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.NotImplementedException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataMapper {

    private DataMapper(){
        // Util class, must not be implemented;
        throw new NotImplementedException();
    }

    public static List<PlayerEntity> mapJsonToPlayerEntity(String json) {
        try {
            List<PlayerEntity> playerEntities = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            JsonNode playerElementsJsonArray = node.get("elements");
            for (JsonNode element : playerElementsJsonArray) {
                PlayerEntity playerEntity = new PlayerEntity();
                playerEntity.setId(element.get("id").asInt());
                playerEntity.setName(element.get("web_name").asText());
                playerEntities.add(playerEntity);
            }
            return playerEntities;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<GameweekEntity> mapJsonToGameweekEntity(String json) {
        try {
            List<GameweekEntity> gameweekEntities = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(json);
            JsonNode playerElementsJsonArray = node.get("events");
            for (JsonNode element : playerElementsJsonArray) {
                GameweekEntity gameweekEntity = new GameweekEntity();
                gameweekEntity.setGameweekNum(element.get("id").asInt());
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(element.get("deadline_time").asText());
                gameweekEntity.setDeadlineTime(zonedDateTime.toLocalDateTime());
                gameweekEntities.add(gameweekEntity);
            }
            return gameweekEntities;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
