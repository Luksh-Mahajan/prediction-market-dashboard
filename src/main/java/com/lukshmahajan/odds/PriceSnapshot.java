package com.lukshmahajan.odds;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class PriceSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marketId;
    private String question;
    private String outcome;
    private double price;
    private Instant capturedAt;

    protected PriceSnapshot() {
    }

    public PriceSnapshot(String marketId, String question, String outcome, double price, Instant capturedAt) {
        this.marketId = marketId;
        this.question = question;
        this.outcome = outcome;
        this.price = price;
        this.capturedAt = capturedAt;
    }

    public Long getId() { return id; }
    public String getMarketId() { return marketId; }
    public String getQuestion() { return question; }
    public String getOutcome() { return outcome; }
    public double getPrice() { return price; }
    public Instant getCapturedAt() { return capturedAt; }
}