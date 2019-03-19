package net.runelite.client.plugins.statstalker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LevelTuple {

    private final int currentLevel;

    private final int opponentLevel;

    private final int xpDifference;

}
