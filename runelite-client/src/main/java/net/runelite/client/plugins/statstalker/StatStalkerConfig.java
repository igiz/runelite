package net.runelite.client.plugins.statstalker;

// Mandatory imports
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("statstalker")
public interface StatStalkerConfig extends Config {

    @ConfigItem(
            keyName = "playerName",
            name = "Player Name",
            description = "The players name you want to stalk stats on.",
            position = 1
    )
    String playerName();

    @ConfigItem(
            position = 2,
            keyName = "refreshInterval",
            name = "Refresh (minutes)",
            description = "Set the time until next refresh"
    )
    default int refreshInterval()
    {
        return 5;
    }

    @ConfigItem(
            position = 3,
            keyName = "showHigher",
            name = "Show Higher",
            description = "Show stats that are higher."
    )
    default boolean showHigher()
    {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName = "showEqual",
            name = "Show Equal",
            description = "Show stats that are equal."
    )
    default boolean showEqual()
    {
        return false;
    }

    @ConfigItem(
            position = 5,
            keyName = "showLower",
            name = "Show Lower",
            description = "Show stats that are lower."
    )
    default boolean showLower()
    {
        return false;
    }

    @ConfigItem(
            position = 6,
            keyName = "showChanged",
            name = "Show Changed",
            description = "Show stats that have changed since snapshot was taken."
    )
    default boolean showChanged()
    {
        return false;
    }

    @ConfigItem(
            position = 7,
            keyName = "takeSnapshots",
            name = "Track progress ",
            description = "Take snapshots to keep track of other players progress."
    )
    default boolean takeSnapshots()
    {
        return false;
    }

    @ConfigItem(
            position = 8,
            keyName = "snapshotInterval",
            name = "Snapshot Interval",
            description = "Intervals to take snapshots at in hours."
    )
    default int snapshotInterval()
    {
        return 24;
    }

}
