package com.lukshmahajan.odds;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PolymarketClient {

    private static final ParameterizedTypeReference<List<Map<String, Object>>> MARKET_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient = RestClient.create("https://gamma-api.polymarket.com");

    public List<Map<String, Object>> fetchTopMarkets(int limit) {
        String now = Instant.now().toString();
        return restClient.get()
                .uri("/markets?limit={limit}&active=true&closed=false"
                        + "&order=volume24hr&ascending=false&end_date_min={now}",
                        limit, now)
                .retrieve()
                .body(MARKET_LIST);
    }

    public List<Map<String, Object>> fetchMarketsByIds(List<String> ids) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/markets")
                        .queryParam("limit", ids.size())
                        .queryParam("id", ids)
                        .build())
                .retrieve()
                .body(MARKET_LIST);
    }
}