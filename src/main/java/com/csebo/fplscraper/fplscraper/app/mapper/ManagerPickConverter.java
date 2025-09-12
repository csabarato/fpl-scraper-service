package com.csebo.fplscraper.fplscraper.app.mapper;

import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickEntity;
import org.SwaggerCodeGenExample.model.CaptainPickDetail;
import org.SwaggerCodeGenExample.model.ManagerPickModel;
import org.SwaggerCodeGenExample.model.PlayerPicksModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManagerPickConverter {

    public static Map<String, CaptainPickDetail> toCaptainPickDetailMap(Map<Integer, ManagerPickEntity> managerCaptainMap) {

        return managerCaptainMap.entrySet().stream()
                .collect(
                Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> {
                            CaptainPickDetail detail = new CaptainPickDetail();
                            detail.setPlayerName(entry.getValue().getPlayer().getName());
                            detail.setMultiplier(entry.getValue().getMultiplier());
                            return detail;
                        }
                )
        );
    }

    public static List<PlayerPicksModel> toPlayerPicksModelList(Map<Integer,List<ManagerPickEntity>> managerPickMap) {

        List<PlayerPicksModel> playerPickList = new ArrayList<>();

        for (Map.Entry<Integer, List<ManagerPickEntity>> entry : managerPickMap.entrySet()) {

            PlayerPicksModel model = new PlayerPicksModel();
            model.setPlayerId(entry.getKey());
            model.setPickedBy(toPlayerPicksModelList(entry.getValue()));
            model.setNumberOfPicks(entry.getValue().size());
            model.setPlayerName(entry.getValue().getFirst().getPlayer().getName());

            playerPickList.add(model);
        }

        playerPickList.sort(Comparator.comparingInt(PlayerPicksModel::getNumberOfPicks).reversed());
        return playerPickList;
    }

    public static List<ManagerPickModel> toPlayerPicksModelList(List<ManagerPickEntity> managerPickList) {
        return managerPickList.stream().map(ManagerPickConverter::toPlayerPicksModelList).toList();
    }

    public static ManagerPickModel toPlayerPicksModelList(ManagerPickEntity managerPickEntity) {
        ManagerPickModel model = new ManagerPickModel();
        model.setId(managerPickEntity.getManagerId());
        model.setMultiplier(managerPickEntity.getMultiplier());
        return model;
    }
}
