package com.lukshmahajan.odds;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, Long> {

    List<PriceSnapshot> findByMarketIdAndCapturedAtGreaterThanEqualOrderByCapturedAtAsc(
            String marketId, Instant since);
}