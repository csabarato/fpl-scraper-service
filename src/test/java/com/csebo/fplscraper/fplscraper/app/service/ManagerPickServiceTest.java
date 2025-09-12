package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickEntity;
import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickRepository;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerEntity;
import org.SwaggerCodeGenExample.model.PicksResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManagerPickServiceTest {

    @Mock
    private ManagerPickRepository managerPickRepository;

    @Mock
    private LeagueDataScraperService leagueDataScraperService;

    @Mock
    private GameweekService gameweekService;

    @InjectMocks
    private ManagerPickService managerPickService;

    @Test
    void getPicksReturnsExistingPicksWhenAllPicksFoundInDatabase() {
        List<Integer> managerIds = List.of(1);
        Integer gameweek = 10;

        List<ManagerPickEntity> existingPicks = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            PlayerEntity player = new PlayerEntity();
            player.setId(200 + i);
            existingPicks.add(createManagerPick(1, gameweek, player, 1, false));
        }

        when(managerPickRepository.findAllByManagerIdIsInAndGameweek(managerIds, gameweek))
            .thenReturn(existingPicks);

        List<ManagerPickEntity> result = managerPickService.getPicks(managerIds, gameweek);

        assertEquals(15, result.size());
        verify(managerPickRepository).findAllByManagerIdIsInAndGameweek(managerIds, gameweek);
        verify(leagueDataScraperService, never()).scrapeAllManagerPicks(anyList(), anyInt());
        verify(managerPickRepository, never()).saveAll(any());
    }

    @Test
    void getPicksScrapesAndSavesWhenNotAllPicksFoundInDatabase() {
        List<Integer> managerIds = Arrays.asList(1, 2);
        Integer gameweek = 10;

        PlayerEntity player = new PlayerEntity();
        player.setId(100);

        List<ManagerPickEntity> partialPicks = List.of(
                createManagerPick(1, gameweek, player, 1, false)
        );

        List<ManagerPickEntity> scrapedPicks = Arrays.asList(
            createManagerPick(1, gameweek, player, 1, false),
            createManagerPick(2, gameweek, player, 1, true)
        );

        when(managerPickRepository.findAllByManagerIdIsInAndGameweek(managerIds, gameweek))
            .thenReturn(partialPicks);
        when(leagueDataScraperService.scrapeAllManagerPicks(managerIds, gameweek))
            .thenReturn(scrapedPicks);

        List<ManagerPickEntity> result = managerPickService.getPicks(managerIds, gameweek);

        assertEquals(2, result.size());
        verify(managerPickRepository).findAllByManagerIdIsInAndGameweek(managerIds, gameweek);
        verify(leagueDataScraperService).scrapeAllManagerPicks(managerIds, gameweek);
        verify(managerPickRepository).saveAll(scrapedPicks);
    }

    @Test
    void getPicksScrapesWhenNoPicksFoundInDatabase() {
        List<Integer> managerIds = List.of(1);
        Integer gameweek = 5;

        PlayerEntity player = new PlayerEntity();
        player.setId(100);

        List<ManagerPickEntity> emptyPicks = List.of();
        List<ManagerPickEntity> scrapedPicks = List.of(
                createManagerPick(1, gameweek, player, 1, true)
        );

        when(managerPickRepository.findAllByManagerIdIsInAndGameweek(managerIds, gameweek))
            .thenReturn(emptyPicks);
        when(leagueDataScraperService.scrapeAllManagerPicks(managerIds, gameweek))
            .thenReturn(scrapedPicks);

        List<ManagerPickEntity> result = managerPickService.getPicks(managerIds, gameweek);

        assertEquals(1, result.size());
        verify(leagueDataScraperService).scrapeAllManagerPicks(managerIds, gameweek);
        verify(managerPickRepository).saveAll(scrapedPicks);
    }

    @Test
    void getPicksResponseBodyUsesCurrentGameweekWhenGameweekIsNull() {
        List<Integer> managerIds = List.of(1);
        Integer currentGameweek = 15;

        PlayerEntity player = new PlayerEntity();
        player.setId(100);
        player.setName("TestPlayer");

        List<ManagerPickEntity> picks = List.of(
                createManagerPick(1, currentGameweek, player, 2, true)
        );

        when(gameweekService.getCurrentGameweek()).thenReturn(currentGameweek);
        when(managerPickRepository.findAllByManagerIdIsInAndGameweek(managerIds, currentGameweek))
            .thenReturn(picks);

        PicksResponseBody result = managerPickService.getPicksResponseBody(managerIds, null);

        assertNotNull(result);
        assertNotNull(result.getPicks());
        assertNotNull(result.getCaptainPicks());
        verify(gameweekService).getCurrentGameweek();
        verify(managerPickRepository).findAllByManagerIdIsInAndGameweek(managerIds, currentGameweek);
    }

    @Test
    void getPicksResponseBodyReturnsCorrectStructureWithCaptainAndNonCaptainPicks() {
        List<Integer> managerIds = Arrays.asList(1, 2);
        Integer gameweek = 10;

        PlayerEntity player1 = new PlayerEntity();
        player1.setId(100);
        player1.setName("Player1");

        PlayerEntity player2 = new PlayerEntity();
        player2.setId(101);
        player2.setName("Player2");

        List<ManagerPickEntity> allPicks = new ArrayList<>();

        allPicks.add(createManagerPick(1, gameweek, player2, 2, true));
        allPicks.add(createManagerPick(2, gameweek, player1, 2, true));

        for (int i = 0; i < 14; i++) {
            PlayerEntity additionalPlayer = new PlayerEntity();
            additionalPlayer.setId(200 + i);
            additionalPlayer.setName("Player" + (200 + i));
            allPicks.add(createManagerPick(1, gameweek, additionalPlayer, 1, false));
            allPicks.add(createManagerPick(2, gameweek, additionalPlayer, 1, false));
        }

        when(managerPickRepository.findAllByManagerIdIsInAndGameweek(managerIds, gameweek))
            .thenReturn(allPicks);

        PicksResponseBody result = managerPickService.getPicksResponseBody(managerIds, gameweek);

        assertNotNull(result);
        assertNotNull(result.getPicks());
        assertNotNull(result.getCaptainPicks());
        assertFalse(result.getPicks().isEmpty());
        assertEquals(2, result.getCaptainPicks().size());
    }

    @Test
    void getPicksResponseBodyHandlesEmptyPicksList() {
        List<Integer> managerIds = List.of(1);
        Integer gameweek = 10;

        when(managerPickRepository.findAllByManagerIdIsInAndGameweek(managerIds, gameweek))
            .thenReturn(List.of());
        when(leagueDataScraperService.scrapeAllManagerPicks(managerIds, gameweek))
            .thenReturn(List.of());

        PicksResponseBody result = managerPickService.getPicksResponseBody(managerIds, gameweek);

        assertNotNull(result);
        assertNotNull(result.getPicks());
        assertNotNull(result.getCaptainPicks());
        assertEquals(0, result.getPicks().size());
        assertEquals(0, result.getCaptainPicks().size());
    }

    private ManagerPickEntity createManagerPick(Integer managerId, Integer gameweek, PlayerEntity player, Integer multiplier, Boolean isCaptain) {
        ManagerPickEntity pick = new ManagerPickEntity();
        pick.setManagerId(managerId);
        pick.setGameweek(gameweek);
        pick.setPlayer(player);
        pick.setMultiplier(multiplier);
        pick.setIsCaptain(isCaptain);
        return pick;
    }
}
