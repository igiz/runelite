package net.runelite.client.plugins.statstalker;

import net.runelite.api.Skill;

public class LevelComparisonTuple {

    public final int currentLevel;

    public final int opponentLevel;

    public final int xpDifference;

    public final Skill skill;

    public LevelComparisonTuple(Skill skill, int currentLevel, int opponentLevel, int xpDifference){
        this.currentLevel = currentLevel;
        this.opponentLevel = opponentLevel;
        this.xpDifference = xpDifference;
        this.skill = skill;
}
}
