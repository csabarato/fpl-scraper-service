package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickEntity;
import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Getter
@Slf4j
public class LeagueDataScraperService {

    private final PlayerService playerService;

    public LeagueDataScraperService(PlayerService playerService) {
        this.playerService = playerService;
    }

    public String getLeagueName(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonRoot = objectMapper.readTree(json);
            return jsonRoot.get("league").get("name").textValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract league name from json: " + json, e);
        }
    }

    public Map<String, String> getManagers(String json) {

        Map<String, String> managers = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode standings = objectMapper.readTree(json);
            for (JsonNode result : standings.get("standings").get("results")){
                managers.put(result.get("entry").asText(), result.get("player_name").asText());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract managers from json: " + json, e);
        }
        return managers;
    }

    public List<ManagerPickEntity> scrapeAllManagerPicks(List<Integer> managerIds, int gameweek) {
        List<Thread> threads = new ArrayList<>();
        List<ManagerPickEntity> managerPicks = Collections.synchronizedList(new ArrayList<>());

        for (Integer managerId : managerIds) {
            Thread thread = Thread.startVirtualThread(() -> {
                    String playerPicksResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/entry/" + managerId + "/event/" + gameweek + "/picks");
                    parsePicksOfManager(managerId, gameweek, playerPicksResponse, managerPicks);
                }
            );
            threads.add(thread);
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
        }

        if (managerPicks.size() != managerIds.size()*15) {
            throw new IllegalStateException("Not all picks were scraped successfully. Expected: " + (managerIds.size()*15) + ", but got: " + managerPicks.size());
        }
        return managerPicks;
    }

    void parsePicksOfManager(Integer managerId, Integer gameweek, String playerPicksJson, List<ManagerPickEntity> managerPicks) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode picks = objectMapper.readTree(playerPicksJson).get("picks");
            if (picks != null) {
                for (JsonNode pick : picks) {
                    int playerId = pick.get("element").asInt();
                    int multiplier = pick.get("multiplier").asInt();
                    Boolean isCaptain = multiplier > 1;
                    ManagerPickEntity managerPickEntity =
                            new ManagerPickEntity(managerId, gameweek, playerService.getById(playerId), multiplier, isCaptain);
                    managerPicks.add(managerPickEntity);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract picks from json: " + playerPicksJson, e);
        }
    }
}
