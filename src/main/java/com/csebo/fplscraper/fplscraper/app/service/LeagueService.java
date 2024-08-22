package com.csebo.fplscraper.fplscraper.app.service;
import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import org.SwaggerCodeGenExample.model.PicksModel;
import org.SwaggerCodeGenExample.model.PlayerPickModel;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.csebo.fplscraper.fplscraper.app.utils.JsonConverterUtils.extractParticipantsFromJson;
import static com.csebo.fplscraper.fplscraper.app.utils.JsonConverterUtils.collectPicksFromJson;

@Service
public class LeagueService {

    private final PlayerService playerService;

    public LeagueService(PlayerService playerService){
        this.playerService = playerService;
    }

    public Map<String, String> scrapeParticipantsFromFplServer(Integer leagueId){
        String jsonResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/leagues-classic/" + leagueId + "/standings");
        return extractParticipantsFromJson(jsonResponse);
    }

    public List<PicksModel> scrapePicksFromFplServer(List<Integer> participantIds, Integer gameweek){
        Map<Integer,List<PlayerPickModel>> picksMap = new HashMap<>();
        for (Integer participantId : participantIds) {
            String jsonResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/entry/" + participantId + "/event/"+ gameweek +"/picks");
            collectPicksFromJson(jsonResponse, picksMap, participantId);
        }
        return mapPicksToListOfPicksModelAndSort(picksMap);
    }

    private List<PicksModel> mapPicksToListOfPicksModelAndSort(Map<Integer,List<PlayerPickModel>> picksMap){

        List<PicksModel> picksModelList = new ArrayList<>();
        for(Map.Entry<Integer,List<PlayerPickModel>> entry : picksMap.entrySet()){
            PicksModel picksModel = new PicksModel();
            picksModel.setPlayerId(entry.getKey());
            picksModel.setPlayerName(playerService.getNameById(entry.getKey()));
            picksModel.setNumberOfPicks(entry.getValue().size());
            picksModel.setPickedBy(entry.getValue());
            picksModelList.add(picksModel);
        }
        picksModelList.sort(Comparator.comparing(PicksModel::getNumberOfPicks).reversed());
        return picksModelList;
    }
}
