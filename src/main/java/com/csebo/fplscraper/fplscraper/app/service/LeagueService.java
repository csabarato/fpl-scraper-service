package com.csebo.fplscraper.fplscraper.app.service;
import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import org.SwaggerCodeGenExample.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LeagueService {

    private final GameweekService gameweekService;
    private final LeagueDataScraperService leagueDataScraperService;

    public LeagueService(GameweekService gameweekService, LeagueDataScraperService leagueDataScraperService){
        this.gameweekService = gameweekService;
        this.leagueDataScraperService = leagueDataScraperService;
    }

    public LeagueDataResponseBody scrapeLeagueDataFromFplServer(Integer leagueId) {
        LeagueDataResponseBody leagueDataResponseModel = new LeagueDataResponseBody();
        String jsonResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/leagues-classic/" + leagueId + "/standings");

        leagueDataResponseModel.setLeagueName(leagueDataScraperService.getLeagueName(jsonResponse));
        leagueDataResponseModel.setManagers(leagueDataScraperService.getManagers(jsonResponse));
        return leagueDataResponseModel;
    }

    public PicksResponseBody getPicksFromFplServer(List<Integer> managerIds, Integer gameweek) {

        PicksResponseBody picksResponseBody = new PicksResponseBody();

        if (gameweek == null) gameweek = gameweekService.getCurrentGameweek();

        leagueDataScraperService.scrapeAllManagerPicks(managerIds, gameweek);

        picksResponseBody.setPicks(leagueDataScraperService.getPlayerPicks());
        picksResponseBody.setCaptainPicks(leagueDataScraperService.getCaptainsMap());
        return picksResponseBody;
    }
}
