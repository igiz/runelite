package net.runelite.client.plugins.statstalker;

import net.runelite.api.Skill;

class LevelTuple {

    public final int currentLevel;

    public final int opponentLevel;

    public final int xpDifference;

    public final Skill skill;

    public LevelTuple(Skill skill, int currentLevel, int opponentLevel, int xpDifference){
        this.currentLevel = currentLevel;
        this.opponentLevel = opponentLevel;
        this.xpDifference = xpDifference;
        this.skill = skill;
    }
}
