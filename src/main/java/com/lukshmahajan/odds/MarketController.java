package com.lukshmahajan.odds;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
public class MarketController {

    private final PriceSnapshotRepository repository;
    private final TrackedMarketRepository trackedRepository;

    public MarketController(PriceSnapshotRepository repository,
                            TrackedMarketRepository trackedRepository) {
        this.repository = repository;
        this.trackedRepository = trackedRepository;
    }

    @GetMapping("/markets/latest")
    public List<TrackedMarket> latest() {
        return trackedRepository.findByActiveTrueOrderByVolumeRankAsc();
    }

    @GetMapping("/markets/{marketId}/prices")
    public List<PriceSnapshot> history(@PathVariable String marketId) {
        return trackedRepository.findById(marketId)
                .filter(TrackedMarket::isActive)
                .map(t -> repository.findByMarketIdAndCapturedAtGreaterThanEqualOrderByCapturedAtAsc(
                        marketId, t.getStreakStartedAt()))
                .orElse(List.of());
    }
}