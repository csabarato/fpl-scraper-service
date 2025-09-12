package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.mapper.ManagerPickConverter;
import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickEntity;
import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickRepository;
import org.SwaggerCodeGenExample.model.CaptainPickDetail;
import org.SwaggerCodeGenExample.model.PicksResponseBody;
import org.SwaggerCodeGenExample.model.PlayerPicksModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ManagerPickService {

    private final ManagerPickRepository managerPickRepository;
    private final LeagueDataScraperService leagueDataScraperService;
    private final GameweekService gameweekService;

    public ManagerPickService(ManagerPickRepository managerPickRepository, LeagueDataScraperService leagueDataScraperService,
                              GameweekService gameweekService) {
        this.managerPickRepository = managerPickRepository;
        this.leagueDataScraperService = leagueDataScraperService;
        this.gameweekService = gameweekService;
    }

    public List<ManagerPickEntity> getPicks(List<Integer> managerIds, Integer gameweek) {
         List<ManagerPickEntity> managerPicks = managerPickRepository.findAllByManagerIdIsInAndGameweek(managerIds, gameweek);

         boolean isAllPicksReturned = managerPicks.size() == managerIds.size() * 15;
         if (!isAllPicksReturned) {
             managerPicks = leagueDataScraperService.scrapeAllManagerPicks(managerIds, gameweek);
             managerPickRepository.saveAll(managerPicks);
         }
         return managerPicks;
    }

    public PicksResponseBody getPicksResponseBody(List<Integer> managerId, Integer gameweek) {

        if (gameweek == null) gameweek = gameweekService.getCurrentGameweek();

        PicksResponseBody picksResponseBody = new PicksResponseBody();
        List<ManagerPickEntity> managerPicks = this.getPicks(managerId, gameweek);

        Map<Integer, List<ManagerPickEntity>> picksByPlayerId = new HashMap<>();
        Map<Integer, ManagerPickEntity> managerCaptainMap = new HashMap<>();

        for (ManagerPickEntity pick : managerPicks) {
            picksByPlayerId.computeIfAbsent(pick.getPlayer().getId(), k -> new ArrayList<>()).add(pick);

            if (pick.getIsCaptain()) {
                managerCaptainMap.put(pick.getManagerId(), pick);
            }
        }

        List<PlayerPicksModel> playerPickList = ManagerPickConverter.toPlayerPicksModelList(picksByPlayerId);
        Map<String, CaptainPickDetail> managerCaptainDetailsMap = ManagerPickConverter.toCaptainPickDetailMap(managerCaptainMap);

        picksResponseBody.setPicks(playerPickList);
        picksResponseBody.setCaptainPicks(managerCaptainDetailsMap);
        return picksResponseBody;
    }
}
