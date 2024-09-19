package com.csebo.fplscraper.fplscraper.app.utils;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;

public class HttpRequestUtils {

    private HttpRequestUtils() {
        throw new NotImplementedException("Util class, should not be instantiated");
    }

    public static String executeGetRequest(String url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ClassicHttpRequest request = new HttpGet(url);
            return httpClient.execute(request,
                    (classicHttpResponse ->{
                        if (classicHttpResponse.getCode() != 200) {
                            throw new IllegalStateException("Failed to execute GET request, status code " + classicHttpResponse.getCode());
                        }
                        return EntityUtils.toString(classicHttpResponse.getEntity());
                    }));
        } catch (IOException e){
            throw new IllegalStateException(e.getMessage());
        }
    }
}
