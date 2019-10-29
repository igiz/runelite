package net.runelite.client.plugins.statstalker;

import java.util.HashMap;

public interface StatComparisonSnapshotService {
    HashMap<String, LevelComparisonTuple> getSnapshot(SkillsGroup group);
    long getTimeTaken(SkillsGroup group);
}
