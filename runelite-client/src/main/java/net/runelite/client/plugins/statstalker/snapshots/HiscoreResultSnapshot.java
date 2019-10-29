package net.runelite.client.plugins.statstalker.snapshots;

import net.runelite.client.plugins.statstalker.config.SnapshotIntervalEnum;
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

    public boolean olderThan(int interval, SnapshotIntervalEnum duration){
        Instant takenAt = Instant.ofEpochSecond(timeTaken);
        Instant timeNow = Instant.now();
        Duration timePassed = Duration.between(takenAt, timeNow);

        boolean result;
        switch(duration) {
            case DAYS:
                result = timePassed.toDays() >= interval;
                break;
            case HOURS:
                result = timePassed.toHours() >= interval;
                break;
            case MINUTES:
                result = timePassed.toMinutes() >= interval;
                break;
            default:
                result = false;
        }

        return result;
    }
}
