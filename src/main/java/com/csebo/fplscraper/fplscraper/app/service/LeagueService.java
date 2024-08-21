package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.scraper.DataScraper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LeagueService {

    public Map<String, String> scrapeParticipantsFromFplServer(Integer leagueId) {
        String jsonResponse = DataScraper.executeGetRequest("https://fantasy.premierleague.com/api/leagues-classic/" + leagueId + "/standings");
        return extractParticipantsFromJson(jsonResponse);
    }

    private Map<String, String> extractParticipantsFromJson(String json) {

        Map<String, String> participants = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode standings = objectMapper.readTree(json);
            for (JsonNode result : standings.get("standings").get("results")){
                participants.put(result.get("entry").asText(), result.get("player_name").asText());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return participants;
    }
}
