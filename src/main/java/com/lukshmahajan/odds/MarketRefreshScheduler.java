package com.lukshmahajan.odds;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MarketRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(MarketRefreshScheduler.class);

    private final MarketSelector selector;
    private final PriceCollector collector;

    public MarketRefreshScheduler(MarketSelector selector, PriceCollector collector) {
        this.selector = selector;
        this.collector = collector;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 */6 * * *")  
    public void refresh() {
        try {
            int count = selector.refreshTop(40);
            int saved = collector.collectTracked();
            log.info("Top-volume refresh done: tracking {} markets, saved {} price rows", count, saved);
        } catch (Exception e) {
            log.error("Top-volume refresh failed", e);
        }
    }
}