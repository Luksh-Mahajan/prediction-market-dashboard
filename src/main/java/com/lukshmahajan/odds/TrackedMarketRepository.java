package com.lukshmahajan.odds;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedMarketRepository extends JpaRepository<TrackedMarket, String> {

    List<TrackedMarket> findByActiveTrueOrderByVolumeRankAsc();
}