package com.lukshmahajan.odds;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketSelector {

    // A market with any outcome priced at or above this is treated as already decided
    private static final double DECIDED_THRESHOLD = 0.97;

    // Fetch extra markets so we still have enough left after removing the decided ones
    private static final int CANDIDATE_MULTIPLIER = 3;

    private final PolymarketClient client;
    private final TrackedMarketRepository repository;

    public MarketSelector(PolymarketClient client, TrackedMarketRepository repository) {
        this.client = client;
        this.repository = repository;
    }

    @Transactional
    public int refreshTop(int limit) {
        Instant now = Instant.now();

        Map<String, TrackedMarket> existing = new HashMap<>();
        for (TrackedMarket m : repository.findAll()) {
            existing.put(m.getMarketId(), m);
        }

        Set<String> topIds = new HashSet<>();
        int rank = 1;

        for (Map<String, Object> market : client.fetchTopMarkets(limit * CANDIDATE_MULTIPLIER)) {
            if (topIds.size() >= limit) {
                break;
            }
            if (isDecided(market)) {
                continue;
            }

            String id = String.valueOf(market.get("id"));
            String question = String.valueOf(market.get("question"));
            if (question.length() > 255) {
                question = question.substring(0, 255);
            }
            topIds.add(id);

            TrackedMarket tracked = existing.get(id);
            if (tracked == null) {
                tracked = new TrackedMarket(id, question, now);   // brand new: streak starts now
            } else if (!tracked.isActive()) {
                tracked.startNewStreak(now);                      // returned after a gap: start fresh
            }
            tracked.setActive(true);
            tracked.setVolumeRank(rank++);
            tracked.setLastSeenAt(now);
            repository.save(tracked);
        }

        // Anything that was tracked but is no longer in the list gets switched off
        for (TrackedMarket m : existing.values()) {
            if (m.isActive() && !topIds.contains(m.getMarketId())) {
                m.setActive(false);
                repository.save(m);
            }
        }
        return topIds.size();
    }

    private boolean isDecided(Map<String, Object> market) {
        for (String price : PriceCollector.parseList(market.get("outcomePrices"))) {
            if (Double.parseDouble(price) >= DECIDED_THRESHOLD) {
                return true;
            }
        }
        return false;
    }
}