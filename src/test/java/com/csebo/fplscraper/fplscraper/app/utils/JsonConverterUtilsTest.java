package com.csebo.fplscraper.fplscraper.app.utils;

import org.SwaggerCodeGenExample.model.PlayerPickModel;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonConverterUtilsTest {

    @Test
    void testExtractParticipantsFromJson() {

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

        Map<String, String> result = JsonConverterUtils.extractParticipantsFromJson(jsonString);
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

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> JsonConverterUtils.extractParticipantsFromJson(jsonString));
        assertTrue(e.getMessage().startsWith("Failed to extract participants from json"));
    }

    @Test
    void testCollectPicksFromJson() {

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

        Map<Integer, List<PlayerPickModel>> picksMap = new HashMap<>();
        JsonConverterUtils.collectPicksFromJson(jsonString,picksMap, 123);

        assertEquals(4, picksMap.size());
        List<PlayerPickModel> playerPicks = picksMap.get(413);
        assertEquals(1, playerPicks.size());
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

        Map<Integer, List<PlayerPickModel>> picksMap = new HashMap<>();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, ()-> JsonConverterUtils.collectPicksFromJson(jsonString,picksMap, 123));
        assertTrue(ex.getMessage().startsWith("Failed to extract picks from json"));
    }
}
