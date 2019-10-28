package net.runelite.client.plugins.statstalker.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SnapshotIntervalEnum {

    HOURS("Hours"),
    DAYS("Days"),
    MINUTES("Minutes");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}
