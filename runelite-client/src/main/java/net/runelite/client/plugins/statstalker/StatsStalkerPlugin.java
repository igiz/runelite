package net.runelite.client.plugins.statstalker;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.ExperienceChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.interfaces.Repository;
import net.runelite.client.plugins.statstalker.config.StatStalkerConfig;
import net.runelite.client.plugins.statstalker.overlay.StatsStalkerOverlay;
import net.runelite.client.plugins.statstalker.snapshots.HiscoreResultSnapshot;
import net.runelite.client.plugins.statstalker.snapshots.JsonFileSnapshotRepository;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.hiscore.*;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@PluginDescriptor(
        name = "Stats Stalker",
        description = "Stalk other players statistics compared to yours.",
        enabledByDefault = false
)
public class StatsStalkerPlugin extends Plugin implements StatComparisonSnapshotService{

    //region Private Fields

    private final Map<HiscoreSkill, net.runelite.api.Skill> skillMap = generateSkillMap();

    private final HiscoreClient hiscoreClient = new HiscoreClient();

    private HiscoreResult result;

    private HiscoreResultSnapshot snapshot;

    private Instant lastRefresh;

    private Repository<HiscoreResultSnapshot> snapshotRepository;

    @Inject
    private StatComparisonSnapshot statSnapshot;

    @Inject
    private StatStalkerConfig config;

    @Inject
    private Client client;

    @Inject
    private SkillIconManager skillIconManager;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private StatsStalkerOverlay overlay;

    //endregion

    //region Public Constructors

    @Inject
    public StatsStalkerPlugin(){
        this.lastRefresh = Instant.MIN;
    }

    //endregion

    @Provides
    StatStalkerConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(StatStalkerConfig.class);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        if(snapshot == null || snapshot.getSnapshot().getPlayer() != config.playerName()) {
            readSnapshot();
        }
        reload();
    }

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        snapshotRepository = new JsonFileSnapshotRepository();
        readSnapshot();
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onExperienceChanged(ExperienceChanged event) {
        resetComparison();
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        Duration timeSinceReload = Duration.between(lastRefresh, Instant.now());
        Duration reloadInterval = Duration.ofMinutes(config.refreshInterval());
        if (timeSinceReload.compareTo(reloadInterval) >= 0) {
            reload();
        }
    }

    //region Reload Logic

    private void reload() {

        CompletableFuture.supplyAsync(() -> {
            try {
                HiscoreResult hiScore = hiscoreClient.lookup(config.playerName(), HiscoreEndpoint.NORMAL);
                return hiScore;
            } catch (IOException e) {
                return null;
            }
        }).thenAccept(hiscoreResult -> {this.result = hiscoreResult; takeSnapshot();})
                .exceptionally(throwable ->
                        {
                            throwable.printStackTrace();
                            return null;
                        })
                .thenRun(() -> {resetComparison(); calculateChanged();});
    }

    //endregion

    //region Stats Gathering

    private void resetComparison() {
        if (result != null) {
            HashMap<String, LevelComparisonTuple> greater = new HashMap<>();
            HashMap<String, LevelComparisonTuple> lower = new HashMap<>();
            HashMap<String, LevelComparisonTuple> equal = new HashMap<>();

            net.runelite.api.Skill[] allSkills = ArrayUtils.removeElement(net.runelite.api.Skill.values(), net.runelite.api.Skill.OVERALL);
            for (int i = 0; i < allSkills.length; i++) {
                net.runelite.api.Skill current = allSkills[i];
                HiscoreSkill highScoreSkill = HiscoreSkill.valueOf(current.getName().toUpperCase());
                Skill opponentsSkill = result.getSkill(highScoreSkill);
                int currentLevel = client.getRealSkillLevel(current);
                int opponentLevel = opponentsSkill.getLevel();
                int xpDifference = Math.toIntExact(client.getSkillExperience(current) - opponentsSkill.getExperience());
                LevelComparisonTuple levelComparisonTuple = new LevelComparisonTuple(current, currentLevel, opponentLevel, xpDifference);

                if (currentLevel > opponentLevel) {
                    greater.put(current.getName(), levelComparisonTuple);
                } else if (currentLevel < opponentLevel) {
                    lower.put(current.getName(), levelComparisonTuple);
                } else {
                    equal.put(current.getName(), levelComparisonTuple);
                }
            }

            long takenAt = Instant.now().getEpochSecond();

            //Push all the data
            statSnapshot.push(greater, SkillsGroup.HIGHER, takenAt);
            statSnapshot.push(lower, SkillsGroup.LOWER, takenAt);
            statSnapshot.push(equal, SkillsGroup.EQUAL, takenAt);
            lastRefresh = Instant.now();
        }
    }

    private void calculateChanged() {
        if (snapshot != null) {
            HashMap<String, LevelComparisonTuple> changedSince = new HashMap<>();
            HiscoreSkill[] allSkills = HiscoreSkill.values();
            for (int i = 0; i < allSkills.length; i++) {
                HiscoreSkill skill = allSkills[i];
                Skill current = result.getSkill(skill);
                Skill before = snapshot.getSnapshot().getSkill(skill);
                boolean canCompare = current != null && before != null && skillMap.containsKey(skill);
                if (canCompare && (current.getExperience() > before.getExperience())) {
                    try {
                        int xpDifference = Math.toIntExact(current.getExperience() - before.getExperience());
                        net.runelite.api.Skill playerSkill = skillMap.get(skill);
                        LevelComparisonTuple levelComparisonTuple = new LevelComparisonTuple(playerSkill, current.getLevel(), before.getLevel(), xpDifference);
                        changedSince.put(playerSkill.getName(), levelComparisonTuple);
                    } catch (IllegalArgumentException ex) {
                        // Do nothing we just can't compare this skill
                    }
                }
            }
            statSnapshot.push(changedSince, SkillsGroup.CHANGED_SINCE_SNAPSHOT, snapshot.getTimeTaken());
        }
    }

    //endregion

    //region Snapshot Management

    private void readSnapshot(){
        snapshot = snapshotRepository.get(config.playerName());
        snapshotChanged();
    }

    private void snapshotChanged(){
        if(snapshot != null && result != null){
            calculateChanged();
        }
    }

    private void takeSnapshot(){
        boolean haveResult = result != null;
        boolean saveSnapshot = config.takeSnapshots();
        boolean expired = snapshot == null || snapshot.olderThan(config.snapshotInterval(), config.snapshotIntervalDuration());
        if(saveSnapshot &&  haveResult && expired){
            snapshot = new HiscoreResultSnapshot(result);
            snapshotRepository.save(snapshot, config.playerName());
            snapshotChanged();
        }
    }

    @Override
    public HashMap<String, LevelComparisonTuple> getSnapshot(SkillsGroup group) {
        return statSnapshot.getSkillsGroup(group);
    }

    @Override
    public long getTimeTaken(SkillsGroup group) {
        return statSnapshot.getTimeTaken(group);
    }

    //endregion

    //region Other
    private Map<HiscoreSkill,net.runelite.api.Skill> generateSkillMap() {
        Map<HiscoreSkill,net.runelite.api.Skill> result = new HashMap<>();
        HiscoreSkill[] allConstants = HiscoreSkill.class.getEnumConstants();
        for(net.runelite.api.Skill playerSkill : net.runelite.api.Skill.class.getEnumConstants()){
            for(HiscoreSkill hiscoreSkill : allConstants){
                if(hiscoreSkill.getName().equals(playerSkill.getName())){
                    result.put(hiscoreSkill, playerSkill);
                    break;
                }
            }
        }

        return result;
    }

    //endregion
}