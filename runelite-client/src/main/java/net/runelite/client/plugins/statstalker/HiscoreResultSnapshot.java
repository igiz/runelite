package net.runelite.client.plugins.statstalker;

import net.runelite.http.api.hiscore.HiscoreResult;

import java.time.Duration;
import java.time.Instant;

public class HiscoreResultSnapshot {

    private long timeTaken;

    private HiscoreResult snapshot;

    public HiscoreResultSnapshot(HiscoreResult snapshot){
        this.timeTaken = Instant.now().getEpochSecond();
        this.snapshot = snapshot;
    }

    public HiscoreResult getSnapshot(){
        return snapshot;
    }

    public long getTimeTaken(){
        return timeTaken;
    }

    public boolean olderThan(int timeInHours){
        Instant takenAt = Instant.ofEpochSecond(timeTaken);
        Instant timeNow = Instant.now();
        Duration duration = Duration.between(takenAt, timeNow);
        return duration.toHours() >= timeInHours;
    }
}
