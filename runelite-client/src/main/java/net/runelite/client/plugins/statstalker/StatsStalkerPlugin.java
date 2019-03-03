package net.runelite.client.plugins.statstalker;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.ExperienceChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.hiscore.*;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@PluginDescriptor(
        name = "Stats Stalker",
        description = "Stalk other players statistics compared to yours.",
        enabledByDefault = false
)
public class StatsStalkerPlugin extends Plugin {

    class LevelTuple {

        public final int currentLevel;

        public final int opponentLevel;

        public final int xpDifference;

        public LevelTuple(int currentLevel, int opponentLevel, int xpDifference){
            this.currentLevel = currentLevel;
            this.opponentLevel = opponentLevel;
            this.xpDifference = xpDifference;
        }
    }

    enum SkillsGroup {
        EQUAL,
        LOWER,
        HIGHER
    }

    interface DataProvider<K,V> {
        HashMap<K,V> getData();
    }

    private final HashMap<net.runelite.api.Skill,LevelTuple> greaterSkills;

    private final HashMap<net.runelite.api.Skill,LevelTuple> lowerSkills;

    private final HashMap<net.runelite.api.Skill,LevelTuple> equalSkills;

    private final HiscoreClient hiscoreClient = new HiscoreClient();

    private HiscoreResult result;

    private Instant lastRefresh;

    @Inject
    private StatStalkerConfig config;

    @Inject
    private Client client;

    @Inject
    private StatsStalkerOverlay higherOverlay;

    @Inject
    private StatsStalkerOverlay lowerOverlay;

    @Inject
    private StatsStalkerOverlay equalOverlay;

    @Inject
    private OverlayManager overlayManager;

    public StatsStalkerPlugin(){
        this.greaterSkills = new HashMap<>();
        this.lowerSkills = new HashMap<>();
        this.equalSkills = new HashMap<>();
        this.lastRefresh = Instant.MIN;
    }

    @Provides
    StatStalkerConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(StatStalkerConfig.class);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        reload();
    }

    public HashMap<net.runelite.api.Skill,LevelTuple> getSkillsGroup(SkillsGroup skillsGroup){
        HashMap<net.runelite.api.Skill,LevelTuple> result = null;
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
        }
        return result;
    }

    public boolean Visible(){
        return result != null;
    }

    @Override
    protected void startUp() throws Exception
    {
        higherOverlay.setColor(Color.GREEN);
        lowerOverlay.setColor(Color.RED);
        equalOverlay.setColor(Color.ORANGE);

        higherOverlay.setDataProvider(() -> getSkillsGroup(SkillsGroup.HIGHER));
        lowerOverlay.setDataProvider(() -> getSkillsGroup(SkillsGroup.LOWER));
        equalOverlay.setDataProvider(() -> getSkillsGroup(SkillsGroup.EQUAL));

        higherOverlay.setTitle("Higher Stats:");
        lowerOverlay.setTitle("Lower Stats:");
        equalOverlay.setTitle("Equal Stats:");

        overlayManager.add(higherOverlay);
        overlayManager.add(lowerOverlay);
        overlayManager.add(equalOverlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(higherOverlay);
        overlayManager.remove(lowerOverlay);
        overlayManager.remove(equalOverlay);
    }

    @Subscribe
    public void onExperienceChanged(ExperienceChanged event) {
        resetComparison();
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        Duration timeSinceInfobox = Duration.between(lastRefresh, Instant.now());
        Duration statTimeout = Duration.ofMinutes(config.refreshInterval());
        if (timeSinceInfobox.compareTo(statTimeout) >= 0) {
            reload();
        }
    }

    private void reload() {

        CompletableFuture.supplyAsync(() -> {
            try {
                HiscoreResult hiScore = hiscoreClient.lookup(config.playerName(), HiscoreEndpoint.NORMAL);
                return hiScore;
            } catch (IOException e) {
                return null;
            }
        }).thenAccept(hiscoreResult -> setResult(hiscoreResult))
                .exceptionally(throwable ->
                        {
                            throwable.printStackTrace();
                            return null;
                        })
                .thenRun(() -> resetComparison());
    }

    private synchronized void setResult(HiscoreResult result){
        this.result = result;
    }

    private synchronized void resetComparison() {
        if (result != null) {
            lowerSkills.clear();
            greaterSkills.clear();
            equalSkills.clear();

            net.runelite.api.Skill[] allSkills = ArrayUtils.removeElement(net.runelite.api.Skill.values(), net.runelite.api.Skill.OVERALL);
            for (int i = 0; i < allSkills.length; i++) {
                net.runelite.api.Skill current = allSkills[i];
                HiscoreSkill highScoreSkill = HiscoreSkill.valueOf(current.getName().toUpperCase());
                Skill opponentsSkill = result.getSkill(highScoreSkill);
                int currentLevel = client.getRealSkillLevel(current);
                int opponentLevel = opponentsSkill.getLevel();
                int xpDifference = Math.toIntExact(Math.abs(opponentsSkill.getExperience() - client.getSkillExperience(current)));
                LevelTuple levelTuple = new LevelTuple(currentLevel, opponentLevel, xpDifference);

                if (currentLevel > opponentLevel) {
                    greaterSkills.put(current, levelTuple);
                } else if (currentLevel < opponentLevel) {
                    lowerSkills.put(current, levelTuple);
                } else {
                    equalSkills.put(current, levelTuple);
                }
            }
            lastRefresh = Instant.now();
        }
    }
}
