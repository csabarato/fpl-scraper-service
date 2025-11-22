package com.csebo.fplscraper.fplscraper.app.controller;

import com.csebo.fplscraper.fplscraper.app.service.LeagueService;
import com.csebo.fplscraper.fplscraper.app.service.ManagerPickService;
import org.SwaggerCodeGenExample.api.LeagueApi;
import org.SwaggerCodeGenExample.model.LeagueDataResponseBody;
import org.SwaggerCodeGenExample.model.PicksRequestBody;
import org.SwaggerCodeGenExample.model.PicksResponseBody;
import org.SwaggerCodeGenExample.model.TransfersResponseBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;


@Controller
public class LeagueController implements LeagueApi {

    private final LeagueService leagueService;
    private final ManagerPickService managerPickService;

    public LeagueController(LeagueService leagueService, ManagerPickService managerPickService) {
        this.leagueService = leagueService;
        this.managerPickService = managerPickService;
    }

    @Override
    public ResponseEntity<LeagueDataResponseBody> getLeagueData(Integer leagueId) {
        LeagueDataResponseBody leagueData = leagueService.scrapeLeagueDataFromFplServer(leagueId);
        return ResponseEntity.ok(leagueData);
    }

    @Override
    public ResponseEntity<PicksResponseBody> getPicks(PicksRequestBody picksRequestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return new ResponseEntity<>(managerPickService.getPicksResponseBody(picksRequestBody.getPlayerIds(),
                picksRequestBody.getGameweek()), headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TransfersResponseBody> getTransfers(PicksRequestBody picksRequestBody) {

        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
        headers.setPragma("no-cache");
        headers.setExpires(0);

        return new ResponseEntity<>(managerPickService.getTransfersResponseBody(picksRequestBody.getPlayerIds(),
                picksRequestBody.getGameweek()), headers, HttpStatus.OK);
    }
}
