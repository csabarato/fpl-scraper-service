package com.csebo.fplscraper.fplscraper.app.mapper;

import com.csebo.fplscraper.fplscraper.app.repository.PlayerEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class PlayerMapper {

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

}
