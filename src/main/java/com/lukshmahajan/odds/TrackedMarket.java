package com.lukshmahajan.odds;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class TrackedMarket {

    @Id
    private String marketId;

    private String question;
    private boolean active;
    private int volumeRank;
    private Instant streakStartedAt;
    private Instant lastSeenAt;

    protected TrackedMarket() {
    }

    public TrackedMarket(String marketId, String question, Instant now) {
        this.marketId = marketId;
        this.question = question;
        this.active = true;
        this.streakStartedAt = now;
        this.lastSeenAt = now;
    }

    public void startNewStreak(Instant now) {
        this.streakStartedAt = now;
    }

    public String getMarketId() { return marketId; }
    public String getQuestion() { return question; }
    public boolean isActive() { return active; }
    public int getVolumeRank() { return volumeRank; }
    public Instant getStreakStartedAt() { return streakStartedAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }

    public void setActive(boolean active) { this.active = active; }
    public void setVolumeRank(int volumeRank) { this.volumeRank = volumeRank; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}