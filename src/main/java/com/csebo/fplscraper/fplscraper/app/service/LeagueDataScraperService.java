package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.SwaggerCodeGenExample.model.CaptainPickDetail;
import org.SwaggerCodeGenExample.model.ManagerPickModel;
import org.SwaggerCodeGenExample.model.PlayerPicksModel;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@Getter
@Slf4j
public class LeagueDataScraperService {

    private final PlayerService playerService;

    private List<PlayerPicksModel> playerPicks = new ArrayList<>();
    private final ConcurrentHashMap<String, CaptainPickDetail> captainsMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, List<ManagerPickModel>> picksMap = new ConcurrentHashMap<>();

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

    public void scrapeAllManagerPicks(List<Integer> managerIds, int gameweek) {
        if (!picksMap.isEmpty()) picksMap.clear();
        if (!captainsMap.isEmpty()) captainsMap.clear();

        List<Thread> threads = new ArrayList<>();

        for (Integer managerId : managerIds) {
            Thread thread = Thread.startVirtualThread(() -> {
                    String playerPicksResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/entry/" + managerId + "/event/" + gameweek + "/picks");
                    scrapePicksOfManager(managerId, playerPicksResponse);
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

        playerPicks = mapPicksMapToListOfPicksModelAndSort(picksMap);
    }

    public void scrapePicksOfManager(Integer managerId, String playerPicksJson) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode picks = objectMapper.readTree(playerPicksJson).get("picks");
            if (picks != null) {
                for (JsonNode pick : picks) {

                    ManagerPickModel managerPickModel =
                            createManagerPickModel(managerId, pick.get("multiplier").asInt());

                    int playerId = pick.get("element").asInt();
                    picksMap.computeIfPresent(playerId, (k, v) -> {
                        v.add(managerPickModel);
                        return v;
                    });

                    picksMap.computeIfAbsent(playerId, k -> {
                        List<ManagerPickModel> managerPlayerPicks = new ArrayList<>();
                        managerPlayerPicks.add(managerPickModel);
                        return managerPlayerPicks;
                    });

                    if (isPlayerCaptain(managerPickModel)) {
                        captainsMap.put(managerId.toString(),
                                new CaptainPickDetail().multiplier(
                                                managerPickModel.getMultiplier())
                                        .playerName(playerService.getNameById(playerId)));
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to extract picks from json: " + playerPicksJson, e);
        }
    }

    private List<PlayerPicksModel> mapPicksMapToListOfPicksModelAndSort(Map<Integer,List<ManagerPickModel>> picksMap){

        List<PlayerPicksModel> picksModelList = new ArrayList<>();
        for(Map.Entry<Integer,List<ManagerPickModel>> entry : picksMap.entrySet()){
            PlayerPicksModel picksModel = new PlayerPicksModel();
            picksModel.setPlayerId(entry.getKey());
            picksModel.setPlayerName(playerService.getNameById(entry.getKey()));
            picksModel.setNumberOfPicks(entry.getValue().size());
            picksModel.setPickedBy(entry.getValue());
            picksModelList.add(picksModel);
        }
        picksModelList.sort(Comparator.comparing(PlayerPicksModel::getNumberOfPicks).reversed());
        return picksModelList;
    }

    private static ManagerPickModel createManagerPickModel(Integer managerId, Integer multiplier) {
        return new ManagerPickModel().id(managerId).multiplier(multiplier);
    }

    private static boolean isPlayerCaptain (ManagerPickModel pick) {
        return pick.getMultiplier() >= 2;
    }

}
