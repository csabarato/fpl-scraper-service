package com.csebo.fplscraper.fplscraper.app.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.SwaggerCodeGenExample.model.PlayerPickModel;
import org.apache.commons.lang3.NotImplementedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConverterUtils {

    private JsonConverterUtils() {
        throw new NotImplementedException("Util class, should not be instantiated");
    }

    public static String extractLeagueNameFromJson(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonRoot = objectMapper.readTree(json);
            return jsonRoot.get("league").get("name").textValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract league name from json: " + json, e);
        }
    }

    public static Map<String, String> extractParticipantsFromJson(String json) {

        Map<String, String> participants = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode standings = objectMapper.readTree(json);
            for (JsonNode result : standings.get("standings").get("results")){
                participants.put(result.get("entry").asText(), result.get("player_name").asText());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract participants from json: " + json, e);
        }
        return participants;
    }

    public static void collectPicksFromJson(String json, Map<Integer, List<PlayerPickModel>> picksMap, Integer participantId) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode picks = objectMapper.readTree(json).get("picks");
            if (picks != null) {
                for (JsonNode pick : picks) {

                    int playerId = pick.get("element").asInt();
                    picksMap.computeIfPresent(playerId, (k, v) -> {
                        PlayerPickModel playerPick = new PlayerPickModel();
                        playerPick.setId(participantId);
                        playerPick.setMultiplier(pick.get("multiplier").asInt());
                        v.add(playerPick);
                        return v;
                    });

                    picksMap.computeIfAbsent(playerId, k -> {
                        PlayerPickModel playerPick = new PlayerPickModel();
                        List<PlayerPickModel> playerPicks = new ArrayList<>();
                        playerPick.id(participantId);
                        playerPick.setMultiplier(pick.get("multiplier").asInt());
                        playerPicks.add(playerPick);
                        return playerPicks;
                    });
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract picks from json: " + json, e);
        }
    }
}
