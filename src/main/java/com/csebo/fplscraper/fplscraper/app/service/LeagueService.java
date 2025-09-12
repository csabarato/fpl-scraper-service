package com.csebo.fplscraper.fplscraper.app.service;
import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import org.SwaggerCodeGenExample.model.*;
import org.springframework.stereotype.Service;

@Service
public class LeagueService {

    private final LeagueDataScraperService leagueDataScraperService;

    public LeagueService(LeagueDataScraperService leagueDataScraperService){
        this.leagueDataScraperService = leagueDataScraperService;
    }

    public LeagueDataResponseBody scrapeLeagueDataFromFplServer(Integer leagueId) {
        LeagueDataResponseBody leagueDataResponseModel = new LeagueDataResponseBody();
        String jsonResponse = HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/leagues-classic/" + leagueId + "/standings");

        leagueDataResponseModel.setLeagueName(leagueDataScraperService.getLeagueName(jsonResponse));
        leagueDataResponseModel.setManagers(leagueDataScraperService.getManagers(jsonResponse));
        return leagueDataResponseModel;
    }
}
