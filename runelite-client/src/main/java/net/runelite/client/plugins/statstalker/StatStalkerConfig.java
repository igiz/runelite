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

}
