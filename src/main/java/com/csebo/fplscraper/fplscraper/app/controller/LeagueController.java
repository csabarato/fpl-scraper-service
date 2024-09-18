package com.csebo.fplscraper.fplscraper.app.controller;

import com.csebo.fplscraper.fplscraper.app.service.LeagueService;
import org.SwaggerCodeGenExample.api.LeagueApi;
import org.SwaggerCodeGenExample.model.LeagueDataModel;
import org.SwaggerCodeGenExample.model.PicksRequestBody;
import org.SwaggerCodeGenExample.model.PicksResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


@Controller
public class LeagueController implements LeagueApi {

    private final LeagueService leagueService;

    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @Override
    public ResponseEntity<LeagueDataModel> getLeagueData(Integer leagueId) {
        LeagueDataModel leagueData = leagueService.scrapeLeagueDataFromFplServer(leagueId);
        return ResponseEntity.ok(leagueData);
    }

    @Override
    public ResponseEntity<PicksResponseBody> getPicks(PicksRequestBody picksRequestBody) {
        return ResponseEntity.ok()
                .body(new PicksResponseBody()
                .picks(leagueService.scrapePicksFromFplServer(picksRequestBody.getPlayerIds(), picksRequestBody.getGameweek())));
    }
}
