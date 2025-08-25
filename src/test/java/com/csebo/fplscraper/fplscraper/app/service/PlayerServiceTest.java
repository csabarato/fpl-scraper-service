package com.csebo.fplscraper.fplscraper.app.service;

import com.csebo.fplscraper.fplscraper.app.mapper.JsonDataMapper;
import com.csebo.fplscraper.fplscraper.app.repository.GameweekRepository;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerEntity;
import com.csebo.fplscraper.fplscraper.app.repository.PlayerRepository;
import com.csebo.fplscraper.fplscraper.app.utils.HttpRequestUtils;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameweekRepository gameweekRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void getNameByIdReturnsPlayerNameWhenPlayerExistsInDatabase() {
        PlayerEntity player = new PlayerEntity();
        player.setId(1);
        player.setName("Salah");

        when(playerRepository.findById(1)).thenReturn(Optional.of(player));

        String result = playerService.getNameById(1);

        assertEquals("Salah", result);
        verify(playerRepository).findById(1);
        verifyNoMoreInteractions(playerRepository);
    }

    @Test
    void getNameByIdFetchesPlayerFromApiAndSavesWhenNotInDatabase() {
        String jsonResponse = """
            {
                "elements": [
                    {
                        "id": 1,
                        "web_name": "Salah"
                    }
                ]
            }
            """;

        PlayerEntity fetchedPlayer = new PlayerEntity();
        fetchedPlayer.setId(1);
        fetchedPlayer.setName("Salah");

        PlayerEntity savedPlayer = new PlayerEntity();
        savedPlayer.setId(1);
        savedPlayer.setName("Salah");

        when(playerRepository.findById(1)).thenReturn(Optional.empty());
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(savedPlayer);

        try (MockedStatic<HttpRequestUtils> httpMock = mockStatic(HttpRequestUtils.class);
             MockedStatic<JsonDataMapper> mapperMock = mockStatic(JsonDataMapper.class)) {

            httpMock.when(() -> HttpRequestUtils.executeGetRequest(anyString()))
                    .thenReturn(jsonResponse);
            mapperMock.when(() -> JsonDataMapper.findPlayerByIdInJson(jsonResponse, 1))
                     .thenReturn(Optional.of(fetchedPlayer));

            String result = playerService.getNameById(1);

            assertEquals("Salah", result);
            verify(playerRepository).findById(1);
            verify(playerRepository).save(fetchedPlayer);
        }
    }

    @Test
    void getNameByIdThrowsEntityNotFoundExceptionWhenPlayerNotFoundInApi() {
        String jsonResponse = """
            {
                "elements": []
            }
            """;

        when(playerRepository.findById(999)).thenReturn(Optional.empty());

        try (MockedStatic<HttpRequestUtils> httpMock = mockStatic(HttpRequestUtils.class);
             MockedStatic<JsonDataMapper> mapperMock = mockStatic(JsonDataMapper.class)) {

            httpMock.when(() -> HttpRequestUtils.executeGetRequest(anyString()))
                    .thenReturn(jsonResponse);
            mapperMock.when(() -> JsonDataMapper.findPlayerByIdInJson(jsonResponse, 999))
                     .thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> playerService.getNameById(999));

            assertTrue(exception.getMessage().contains("Player data not found"));
            verify(playerRepository).findById(999);
            verify(playerRepository, never()).save(any());
        }
    }

    @Test
    void saveDataFromFplServerFetchesAndSavesPlayerAndGameweekData() {
        String jsonResponse = """
            {
                "elements": [
                    {
                        "id": 1,
                        "web_name": "Salah"
                    }
                ],
                "events": [
                    {
                        "id": 1,
                        "deadline_time": "2024-08-17T10:00:00Z"
                    }
                ]
            }
            """;

        try (MockedStatic<HttpRequestUtils> httpMock = mockStatic(HttpRequestUtils.class);
             MockedStatic<JsonDataMapper> mapperMock = mockStatic(JsonDataMapper.class)) {

            httpMock.when(() -> HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/bootstrap-static/"))
                    .thenReturn(jsonResponse);
            mapperMock.when(() -> JsonDataMapper.mapJsonToPlayerEntities(jsonResponse))
                     .thenReturn(anyList());
            mapperMock.when(() -> JsonDataMapper.mapJsonToGameweekEntities(jsonResponse))
                     .thenReturn(anyList());

            playerService.saveDataFromFplServer();

            verify(playerRepository).saveAll(anyList());
            verify(gameweekRepository).saveAll(anyList());
            httpMock.verify(() -> HttpRequestUtils.executeGetRequest("https://fantasy.premierleague.com/api/bootstrap-static/"));
        }
    }

    @Test
    void saveDataFromFplServerHandlesHttpRequestFailure() {
        try (MockedStatic<HttpRequestUtils> httpMock = mockStatic(HttpRequestUtils.class)) {
            httpMock.when(() -> HttpRequestUtils.executeGetRequest(anyString()))
                    .thenThrow(new RuntimeException("HTTP request failed"));

            assertThrows(RuntimeException.class, () -> playerService.saveDataFromFplServer());

            verify(playerRepository, never()).saveAll(anyList());
            verify(gameweekRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void fetchMissingPlayerDataSavesPlayerWhenFoundInApiFromFplServer() {
        String jsonResponse = """
            {
                "elements": [
                    {
                        "id": 5,
                        "web_name": "Mane"
                    }
                ]
            }
            """;

        PlayerEntity fetchedPlayer = new PlayerEntity();
        fetchedPlayer.setId(5);
        fetchedPlayer.setName("Mane");

        PlayerEntity savedPlayer = new PlayerEntity();
        savedPlayer.setId(5);
        savedPlayer.setName("Mane");

        when(playerRepository.findById(5)).thenReturn(Optional.empty());
        when(playerRepository.save(fetchedPlayer)).thenReturn(savedPlayer);

        try (MockedStatic<HttpRequestUtils> httpMock = mockStatic(HttpRequestUtils.class);
             MockedStatic<JsonDataMapper> mapperMock = mockStatic(JsonDataMapper.class)) {

            httpMock.when(() -> HttpRequestUtils.executeGetRequest(anyString()))
                    .thenReturn(jsonResponse);
            mapperMock.when(() -> JsonDataMapper.findPlayerByIdInJson(jsonResponse, 5))
                     .thenReturn(Optional.of(fetchedPlayer));

            String result = playerService.getNameById(5);

            assertEquals("Mane", result);
            verify(playerRepository).save(fetchedPlayer);
        }
    }
}
