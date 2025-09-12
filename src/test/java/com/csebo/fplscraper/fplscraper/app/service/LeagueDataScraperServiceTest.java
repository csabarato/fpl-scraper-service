package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.repository.ManagerPickEntity;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeagueDataScraperServiceTest {

    @Mock
    PlayerService playerService;

    @InjectMocks
    LeagueDataScraperService leagueDataScraperService;

    @Test
    void parsePicksOfManagerCreatesCorrectNumberOfEntities() {
        String validJson = """
            {
                "picks": [
                    {"element": 1, "multiplier": 1},
                    {"element": 2, "multiplier": 2},
                    {"element": 3, "multiplier": 1},
                    {"element": 4, "multiplier": 1},
                    {"element": 5, "multiplier": 1}
                ]
            }
            """;

        PlayerEntity mockPlayer = new PlayerEntity();
        mockPlayer.setId(1);
        when(playerService.getById(anyInt())).thenReturn(mockPlayer);

        List<ManagerPickEntity> managerPicks = Collections.synchronizedList(new ArrayList<>());

        leagueDataScraperService.parsePicksOfManager(123, 5, validJson, managerPicks);

        assertEquals(5, managerPicks.size());
    }

    @Test
    void parsePicksOfManagerSetsCorrectManagerIdAndGameweek() {
        String validJson = """
            {
                "picks": [
                    {"element": 100, "multiplier": 1},
                    {"element": 200, "multiplier": 2}
                ]
            }
            """;

        PlayerEntity mockPlayer = new PlayerEntity();
        when(playerService.getById(anyInt())).thenReturn(mockPlayer);

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        leagueDataScraperService.parsePicksOfManager(456, 10, validJson, managerPicks);

        assertEquals(2, managerPicks.size());
        for (ManagerPickEntity pick : managerPicks) {
            assertEquals(456, pick.getManagerId());
            assertEquals(10, pick.getGameweek());
        }
    }

    @Test
    void parsePicksOfManagerIdentifiesCaptainCorrectly() {
        String validJson = """
            {
                "picks": [
                    {"element": 1, "multiplier": 1},
                    {"element": 2, "multiplier": 2},
                    {"element": 3, "multiplier": 3},
                    {"element": 4, "multiplier": 1}
                ]
            }
            """;

        PlayerEntity mockPlayer = new PlayerEntity();
        when(playerService.getById(anyInt())).thenReturn(mockPlayer);

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        leagueDataScraperService.parsePicksOfManager(123, 1, validJson, managerPicks);

        assertEquals(4, managerPicks.size());

        // Check regular picks (multiplier = 1)
        long regularPicks = managerPicks.stream()
            .filter(pick -> pick.getMultiplier() == 1)
            .filter(pick -> !pick.getIsCaptain())
            .count();
        assertEquals(2, regularPicks);

        // Check captain picks (multiplier > 1)
        long captainPicks = managerPicks.stream()
            .filter(pick -> pick.getMultiplier() > 1)
            .filter(ManagerPickEntity::getIsCaptain)
            .count();
        assertEquals(2, captainPicks);
    }

    @Test
    void parsePicksOfManagerSetsCorrectMultiplierValues() {
        String validJson = """
            {
                "picks": [
                    {"element": 1, "multiplier": 1},
                    {"element": 2, "multiplier": 2},
                    {"element": 3, "multiplier": 3}
                ]
            }
            """;

        PlayerEntity mockPlayer = new PlayerEntity();
        when(playerService.getById(anyInt())).thenReturn(mockPlayer);

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        leagueDataScraperService.parsePicksOfManager(123, 1, validJson, managerPicks);

        assertEquals(3, managerPicks.size());

        // Find and verify each multiplier
        ManagerPickEntity multiplier1Pick = managerPicks.stream()
            .filter(pick -> pick.getMultiplier() == 1)
            .findFirst()
            .orElse(null);
        assertNotNull(multiplier1Pick);
        assertFalse(multiplier1Pick.getIsCaptain());

        ManagerPickEntity multiplier2Pick = managerPicks.stream()
            .filter(pick -> pick.getMultiplier() == 2)
            .findFirst()
            .orElse(null);
        assertNotNull(multiplier2Pick);
        assertTrue(multiplier2Pick.getIsCaptain());

        ManagerPickEntity multiplier3Pick = managerPicks.stream()
            .filter(pick -> pick.getMultiplier() == 3)
            .findFirst()
            .orElse(null);
        assertNotNull(multiplier3Pick);
        assertTrue(multiplier3Pick.getIsCaptain());
    }

    @Test
    void parsePicksOfManagerCreatesCorrectPlayerIds() {
        String validJson = """
            {
                "picks": [
                    {"element": 100, "multiplier": 1},
                    {"element": 200, "multiplier": 1},
                    {"element": 300, "multiplier": 2}
                ]
            }
            """;

        PlayerEntity mockPlayer1 = new PlayerEntity();
        mockPlayer1.setId(100);
        PlayerEntity mockPlayer2 = new PlayerEntity();
        mockPlayer2.setId(200);
        PlayerEntity mockPlayer3 = new PlayerEntity();
        mockPlayer3.setId(300);

        when(playerService.getById(100)).thenReturn(mockPlayer1);
        when(playerService.getById(200)).thenReturn(mockPlayer2);
        when(playerService.getById(300)).thenReturn(mockPlayer3);

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        leagueDataScraperService.parsePicksOfManager(123, 1, validJson, managerPicks);

        assertEquals(3, managerPicks.size());

        // Verify player IDs are set correctly in the composite key
        assertTrue(managerPicks.stream().anyMatch(pick -> pick.getPlayer().getId() == 100));
        assertTrue(managerPicks.stream().anyMatch(pick -> pick.getPlayer().getId() == 200));
        assertTrue(managerPicks.stream().anyMatch(pick -> pick.getPlayer().getId() == 300));
    }

    @Test
    void parsePicksOfManagerHandlesEmptyPicksArray() {
        String emptyPicksJson = """
            {
                "picks": []
            }
            """;

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        leagueDataScraperService.parsePicksOfManager(123, 1, emptyPicksJson, managerPicks);

        assertEquals(0, managerPicks.size());
    }

    @Test
    void parsePicksOfManagerHandlesNullPicks() {
        String nullPicksJson = """
            {
                "other_data": "some_value"
            }
            """;

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        leagueDataScraperService.parsePicksOfManager(123, 1, nullPicksJson, managerPicks);

        assertEquals(0, managerPicks.size());
    }

    @Test
    void parsePicksOfManagerThrowsExceptionOnMalformedJson() {
        String malformedJson = """
            {
                "picks": [
                    {"element": 1, "multiplier": 1},
                    {"element": 2, "multiplier": 1}
                // missing closing bracket
            """;

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> leagueDataScraperService.parsePicksOfManager(123, 1, malformedJson, managerPicks));

        assertTrue(exception.getMessage().startsWith("Failed to extract picks from json"));
    }

    @Test
    void parsePicksOfManagerThrowsExceptionOnMissingElementField() {
        String invalidJson = """
            {
                "picks": [
                    {"multiplier": 1},
                    {"element": 2, "multiplier": 1}
                ]
            }
            """;

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> leagueDataScraperService.parsePicksOfManager(123, 1, invalidJson, managerPicks));

        assertTrue(exception.getMessage().startsWith("Failed to extract picks from json"));
    }

    @Test
    void parsePicksOfManagerThrowsExceptionOnMissingMultiplierField() {
        String invalidJson = """
            {
                "picks": [
                    {"element": 1, "multiplier": 1},
                    {"element": 2}
                ]
            }
            """;

        List<ManagerPickEntity> managerPicks = new ArrayList<>();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> leagueDataScraperService.parsePicksOfManager(123, 1, invalidJson, managerPicks));

        assertTrue(exception.getMessage().startsWith("Failed to extract picks from json"));
    }
}