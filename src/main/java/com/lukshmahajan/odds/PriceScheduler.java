package com.lukshmahajan.odds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceScheduler.class);

    private final PriceCollector collector;

    public PriceScheduler(PriceCollector collector) {
        this.collector = collector;
    }

    // Runs at minutes 5, 10, 15 ... 55 of every hour. Minute 0 is handled by the hourly refresh.
    @Scheduled(cron = "0 5/5 * * * *")
    @Scheduled(cron = "0 0 1-5,7-11,13-17,19-23 * * *")
    public void collectPrices() {
        try {
            int saved = collector.collectTracked();
            log.info("Saved {} price rows", saved);
        } catch (Exception e) {
            log.error("Price collection failed", e);
        }
    }
}