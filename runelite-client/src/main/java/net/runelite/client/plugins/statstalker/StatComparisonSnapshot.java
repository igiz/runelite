package net.runelite.client.plugins.statstalker;

import java.util.HashMap;

public class StatComparisonSnapshot {

    private final HashMap<SkillsGroup, HashMap<String, LevelComparisonTuple>> allData;
    private final HashMap<SkillsGroup, Long> timeTaken;

    public StatComparisonSnapshot() {
        this.allData = new HashMap<>();
        this.timeTaken = new HashMap<>();
    }

    public void push(HashMap<String, LevelComparisonTuple> data, SkillsGroup group, long timeTaken){

        if(data == null){
            throw new IllegalArgumentException("The data cannot be null");
        }

        this.allData.put(group, data);
        this.timeTaken.put(group, timeTaken);
    }

    public HashMap<String, LevelComparisonTuple> getSkillsGroup(SkillsGroup skillsGroup){
        HashMap<String, LevelComparisonTuple> result = allData.get(skillsGroup);
        return new HashMap<>(result);
    }

    public long getTimeTaken(SkillsGroup skillsGroup){
        return timeTaken.get(skillsGroup);
    }
}
