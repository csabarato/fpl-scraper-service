package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.mapper.ManagerPickConverter;
import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickEntity;
import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickRepository;
import org.SwaggerCodeGenExample.model.CaptainPickDetail;
import org.SwaggerCodeGenExample.model.PicksResponseBody;
import org.SwaggerCodeGenExample.model.PlayerPicksModel;
import org.SwaggerCodeGenExample.model.TransferredPlayerModel;
import org.SwaggerCodeGenExample.model.TransfersResponseBody;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    Map<Integer, List<ManagerPickEntity>> getPicksGroupedByManagerId(List<Integer> managerIds, Integer gameweek) {
        List<ManagerPickEntity> managerPicks = this.getPicks(managerIds, gameweek);
        return managerPicks.stream().collect(Collectors.groupingBy(ManagerPickEntity::getManagerId));
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

    public TransfersResponseBody getTransfersResponseBody(List<Integer> managerIds, Integer gameweek) {

        int currentGameweek = gameweekService.getCurrentGameweek();
        if (gameweek == null || gameweek > currentGameweek) gameweek = currentGameweek;

        TransfersResponseBody transfersResponseBody = new TransfersResponseBody();

        Map<Integer, List<ManagerPickEntity>> currentPicksByManager = getPicksGroupedByManagerId(managerIds, gameweek);
        Map<Integer, List<ManagerPickEntity>> previousPicksByManager = getPicksGroupedByManagerId(managerIds, gameweek-1);

        Map<String, List<TransferredPlayerModel>> transfers = new HashMap<>();

        // Find transfers for each manager
        for (Integer managerId : managerIds) {
            List<TransferredPlayerModel> transferredPlayers =
                    getTransferredPlayerModelsOfManager(managerId, currentPicksByManager, previousPicksByManager);
            transfers.put(managerId.toString(), transferredPlayers);
        }

        transfersResponseBody.setTransfers(transfers);
        return transfersResponseBody;
    }

    private List<TransferredPlayerModel> getTransferredPlayerModelsOfManager(Integer managerId,
                                                                             Map<Integer, List<ManagerPickEntity>> currentPicksByManager,
                                                                             Map<Integer, List<ManagerPickEntity>> previousPicksByManager) {

        List<ManagerPickEntity> currentPicks = currentPicksByManager.get(managerId);
        List<ManagerPickEntity> previousPicks = previousPicksByManager.get(managerId);

        Set<Integer> currentPlayerIds = currentPicks.stream()
                .map(pick -> pick.getPlayer().getId())
                .collect(Collectors.toSet());

        Set<Integer> previousPlayerIds = previousPicks.stream()
                .map(pick -> pick.getPlayer().getId())
                .collect(Collectors.toSet());

        List<TransferredPlayerModel> transferredPlayers = new ArrayList<>(getTransferredPlayers(currentPicks, previousPlayerIds, true));
        transferredPlayers.addAll(getTransferredPlayers(previousPicks, currentPlayerIds, false));

        return transferredPlayers;
    }

    private List<TransferredPlayerModel> getTransferredPlayers(List<ManagerPickEntity> pickEntities, Set<Integer> otherPlayerIds, boolean isIn) {
        return pickEntities.stream()
                .filter(pick -> !otherPlayerIds.contains(pick.getPlayer().getId()))
                .map(pick -> {
                    TransferredPlayerModel player = new TransferredPlayerModel();
                    player.setPlayerName(pick.getPlayer().getName());
                    player.setIn(isIn);
                    return player;
                })
                .collect(Collectors.toList());
    }
}
