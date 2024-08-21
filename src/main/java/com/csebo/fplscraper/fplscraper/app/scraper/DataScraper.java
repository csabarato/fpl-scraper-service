package com.csebo.fplscraper.fplscraper.app.scraper;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class DataScraper {

    public static String executeGetRequest(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ClassicHttpRequest request = new HttpGet(url);
            return httpClient.execute(request,
                    (classicHttpResponse -> EntityUtils.toString(classicHttpResponse.getEntity())));
        } catch (IOException e){
            throw new IllegalStateException();
        }
    }
}
