package com.csebo.fplscraper.fplscraper.app.utils;

import com.csebo.fplscraper.fplscraper.app.service.LeagueDataScraperService;
import com.csebo.fplscraper.fplscraper.app.service.PlayerService;
import org.SwaggerCodeGenExample.model.ManagerPickModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LeagueDataScraperServiceTest {

    @Mock
    PlayerService playerService;

    @InjectMocks
    LeagueDataScraperService leagueDataScraperService;



    @Test
    void testExtractManagersFromJson() {

        String jsonString = """
                    {
                        "standings": {
                            "results": [
                                {
                                    "id": 32849821,
                                    "player_name": "Test1",
                                    "entry": 3534829
                                },
                                {
                                    "id": 14162702,
                                    "player_name": "Test2",
                                    "entry": 463885
                                }
                            ]
                        }
                    }
                    """;

        Map<String, String> result = leagueDataScraperService.getManagers(jsonString);
        assertEquals(2, result.size());
    }

    @Test
    void testExtractFromCorruptedJson() {
        String jsonString = """
                    {
                        "standings": {
                            "results": [
                                {
                                    "id": 32849821,
                                    "player_name": "Test1",
                                    "entry": 3534829
                                },
                                {
                                    "id": 14162702
                                }
                            ]
                        }
                    }
                    """;

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> leagueDataScraperService.getManagers(jsonString));
        assertTrue(e.getMessage().startsWith("Failed to extract managers from json"));
    }


    @Test
    void testCollectPicksFromJson() {

        String jsonString = """
                {
                    "picks": [
                        {
                            "element": 413,
                            "position": 1,
                            "multiplier": 2,
                            "is_captain": false,
                            "is_vice_captain": false
                        },
                        {
                            "element": 395,
                            "position": 2,
                            "multiplier": 1,
                            "is_captain": false,
                            "is_vice_captain": false
                        },
                        {
                            "element": 339,
                            "position": 3,
                            "multiplier": 1,
                            "is_captain": false,
                            "is_vice_captain": false
                        },
                        {
                            "element": 200,
                            "position": 4,
                            "multiplier": 1,
                            "is_captain": false,
                            "is_vice_captain": false
                        }
                    ]
                }
                """;

        leagueDataScraperService.scrapePicksOfManager(0, jsonString);

        assertEquals(4, leagueDataScraperService.getPicksMap().size());
        List<ManagerPickModel> playerPicks = leagueDataScraperService.getPicksMap().get(413);
        assertEquals(1, playerPicks.size());
        assertEquals(1, leagueDataScraperService.getCaptainsMap().size());
        assertNotNull(leagueDataScraperService.getCaptainsMap().get("0"));
    }

    @Test
    void testCollectMultipleManagerPicksFromJson() {

        String jsonString1 = """
                {
                    "picks": [
                        {
                            "element": 413,
                            "position": 1,
                            "multiplier": 2,
                            "is_captain": false,
                            "is_vice_captain": false
                        },
                        {
                            "element": 395,
                            "position": 2,
                            "multiplier": 1,
                            "is_captain": false,
                            "is_vice_captain": false
                        }
                    ]
                }
                """;

        leagueDataScraperService.scrapePicksOfManager(0, jsonString1);

        assertEquals(2, leagueDataScraperService.getPicksMap().size());
        List<ManagerPickModel> playerPicks = leagueDataScraperService.getPicksMap().get(413);
        assertEquals(1, playerPicks.size());
        assertEquals(1, leagueDataScraperService.getCaptainsMap().size());
        assertNotNull(leagueDataScraperService.getCaptainsMap().get("0"));

        String jsonString2 = """
                {
                    "picks": [
                        {
                            "element": 413,
                            "position": 1,
                            "multiplier": 2,
                            "is_captain": false,
                            "is_vice_captain": false
                        },
                        {
                            "element": 456,
                            "position": 2,
                            "multiplier": 1,
                            "is_captain": false,
                            "is_vice_captain": false
                        }
                    ]
                }
                """;

        leagueDataScraperService.scrapePicksOfManager(1, jsonString2);

        assertEquals(3, leagueDataScraperService.getPicksMap().size());
        playerPicks = leagueDataScraperService.getPicksMap().get(413);
        assertEquals(2, playerPicks.size());
        assertEquals(2, leagueDataScraperService.getCaptainsMap().size());
        assertNotNull(leagueDataScraperService.getCaptainsMap().get("1"));
    }


    @Test
    void testCollectPicksFromCorruptedJson() {

        String jsonString = """
                {
                    "picks": [
                        {
                            "element": 413,
                            "position": 1,
                            "multiplier": 1,
                            "is_captain": false,
                            "is_vice_captain": false
                        },
                        {
                            "element": 395,
                            "position": 2,
                            "multiplier": 1,
                            "is_captain": false,
                            "is_vice_captain": false
                        }
                }
                """;

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()-> leagueDataScraperService.scrapePicksOfManager(123, jsonString));
        assertTrue(ex.getMessage().startsWith("Failed to extract picks from json"));
    }

}
