package com.lukshmahajan.odds;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class PriceCollector {

    private static final Pattern QUOTED = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");

    private final PolymarketClient client;
    private final TrackedMarketRepository trackedRepository;
    private final PriceSnapshotRepository repository;

    public PriceCollector(PolymarketClient client,
                          TrackedMarketRepository trackedRepository,
                          PriceSnapshotRepository repository) {
        this.client = client;
        this.trackedRepository = trackedRepository;
        this.repository = repository;
    }

    public int collectTracked() {
        List<String> ids = new ArrayList<>();
        for (TrackedMarket m : trackedRepository.findByActiveTrueOrderByVolumeRankAsc()) {
            ids.add(m.getMarketId());
        }
        if (ids.isEmpty()) {
            return 0;
        }

        Instant now = Instant.now();
        List<PriceSnapshot> snapshots = new ArrayList<>();

        for (Map<String, Object> market : client.fetchMarketsByIds(ids)) {
            String marketId = String.valueOf(market.get("id"));
            String question = String.valueOf(market.get("question"));
            if (question.length() > 255) {
                question = question.substring(0, 255);
            }

            List<String> outcomes = parseList(market.get("outcomes"));
            List<String> prices = parseList(market.get("outcomePrices"));

            for (int i = 0; i < Math.min(outcomes.size(), prices.size()); i++) {
                double price = Double.parseDouble(prices.get(i));
                snapshots.add(new PriceSnapshot(marketId, question, outcomes.get(i), price, now));
            }
        }

        repository.saveAll(snapshots);
        return snapshots.size();
    }

    // Polymarket sends lists as text like ["Yes", "No"]; this pulls out each quoted item.
        static List<String> parseList(Object raw) {
        List<String> result = new ArrayList<>();
        if (raw == null) {
            return result;
        }
        Matcher matcher = QUOTED.matcher(raw.toString());
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }
}