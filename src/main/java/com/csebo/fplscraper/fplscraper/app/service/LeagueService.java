package com.csebo.fplscraper.fplscraper.app.service;
import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import org.SwaggerCodeGenExample.model.LeagueDataModel;
import org.SwaggerCodeGenExample.model.PicksModel;
import org.SwaggerCodeGenExample.model.PlayerPickModel;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.csebo.fplscraper.fplscraper.app.utils.JsonConverterUtils.*;

@Service
public class LeagueService {

    private final PlayerService playerService;
    private final GameweekService gameweekService;

    public LeagueService(PlayerService playerService, GameweekService gameweekService){
        this.playerService = playerService;
        this.gameweekService = gameweekService;
    }

    public LeagueDataModel scrapeLeagueDataFromFplServer(Integer leagueId) {
        LeagueDataModel leagueDataModel = new LeagueDataModel();
        String jsonResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/leagues-classic/" + leagueId + "/standings");

        leagueDataModel.setLeagueName(extractLeagueNameFromJson(jsonResponse));
        leagueDataModel.setParticipants(extractParticipantsFromJson(jsonResponse));
        return leagueDataModel;
    }

    public List<PicksModel> scrapePicksFromFplServer(List<Integer> participantIds, Integer gameweek){

        if (gameweek == null) gameweek = gameweekService.getCurrentGameweek();

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
