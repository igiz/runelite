package net.runelite.client.plugins.statstalker.modules.comparison;

import net.runelite.api.Client;
import net.runelite.client.plugins.statstalker.LevelTuple;
import net.runelite.client.plugins.statstalker.OverlayGroup;
import net.runelite.client.plugins.statstalker.StatStalkerConfig;
import net.runelite.client.plugins.statstalker.StatsOverlayViewModel;
import net.runelite.client.plugins.statstalker.modules.Module;
import net.runelite.http.api.hiscore.HiscoreResult;
import net.runelite.http.api.hiscore.HiscoreSkill;
import net.runelite.http.api.hiscore.Skill;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HiScoreComparisonModule implements Module {

    private enum SkillsGroup {
        EQUAL,
        LOWER,
        HIGHER,
    }

    private final HashMap<String, LevelTuple> greaterSkills = new HashMap<>();

    private final HashMap<String,LevelTuple> lowerSkills = new HashMap<>();

    private final HashMap<String,LevelTuple> equalSkills = new HashMap<>();

    private HiscoreResult result;

    @Inject
    private Client client;

    public void setResult(HiscoreResult result){
        this.result = result;
    }

    @Override
    public void configChanged(StatStalkerConfig config) {
        // Do nothing for now
    }

    public void refresh() {
        if (result != null) {
            net.runelite.api.Skill[] allSkills = ArrayUtils.removeElement(net.runelite.api.Skill.values(), net.runelite.api.Skill.OVERALL);
            for (int i = 0; i < allSkills.length; i++) {
                net.runelite.api.Skill current = allSkills[i];
                HiscoreSkill highScoreSkill = HiscoreSkill.valueOf(current.getName().toUpperCase());
                Skill opponentsSkill = result.getSkill(highScoreSkill);
                int xpDifference = Math.toIntExact(client.getSkillExperience(current) - opponentsSkill.getExperience());
                int currentLevel = client.getRealSkillLevel(current);
                int opponentLevel = opponentsSkill.getLevel();

                LevelTuple tuple = new LevelTuple(currentLevel, opponentLevel, xpDifference);
                String skill = current.getName();

                if (currentLevel > opponentLevel) {
                    greaterSkills.put(skill, tuple);
                } else if (currentLevel < opponentLevel) {
                    lowerSkills.put(skill, tuple);
                } else {
                    equalSkills.put(skill, tuple);
                }
            }
        }
    }

    @Override
    public List<StatsOverlayViewModel> load(StatStalkerConfig config, OverlayGroup group) {
        List<StatsOverlayViewModel> result = new ArrayList<>(Arrays.asList(new StatsOverlayViewModel[]{
                new StatsOverlayViewModel(() -> config.showHigher(), "Higher Stats:", Color.GREEN, group, () -> getGroup(SkillsGroup.HIGHER)),
                new StatsOverlayViewModel(() -> config.showLower(), "Lower Stats:", Color.RED, group, () -> getGroup(SkillsGroup.LOWER)),
                new StatsOverlayViewModel(() -> config.showEqual(), "Equal Stats:", Color.ORANGE, group, () -> getGroup(SkillsGroup.EQUAL))
        }));

        return result;
    }

    public HashMap<String,LevelTuple> getGroup(SkillsGroup skillsGroup){
        HashMap<String,LevelTuple> result;
        switch(skillsGroup){
            case EQUAL:
                result = equalSkills;
                break;
            case HIGHER:
                result = greaterSkills;
                break;
            case LOWER:
                result = lowerSkills;
                break;
            default:
                throw new UnsupportedOperationException("Unknown skill group");
        }
        return result;
    }

    public void reset(){
        lowerSkills.clear();
        greaterSkills.clear();
        equalSkills.clear();
    }
}
