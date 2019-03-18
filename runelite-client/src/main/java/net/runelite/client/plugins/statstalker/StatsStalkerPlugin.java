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
import net.runelite.client.plugins.interfaces.Repository;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.hiscore.*;
import org.apache.commons.lang3.ArrayUtils;

import javax.inject.Inject;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

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
        HIGHER,
        CHANGED_SINCE_SNAPSHOT
    }

    interface DataProvider<K,V> {
        HashMap<K,V> getData();
    }

    interface Toggle{
        boolean isToggled();
    }

    private final HashMap<String,LevelTuple> greaterSkills;

    private final HashMap<String,LevelTuple> lowerSkills;

    private final HashMap<String,LevelTuple> equalSkills;

    private final HashMap<String,LevelTuple> changedSinceSnapshot;

    private final HiscoreClient hiscoreClient = new HiscoreClient();

    private HiscoreResult result;

    private HiscoreResultSnapshot snapshot;

    private Instant lastRefresh;

    private Repository<HiscoreResultSnapshot> snapshotRepository;

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
    private StatsStalkerOverlay changedSinceOverlay;

    @Inject
    private OverlayManager overlayManager;

    public StatsStalkerPlugin(){
        this.greaterSkills = new HashMap<>();
        this.lowerSkills = new HashMap<>();
        this.equalSkills = new HashMap<>();
        this.changedSinceSnapshot = new HashMap<>();

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
        if(snapshot == null || snapshot.getSnapshot().getPlayer() != config.playerName()) {
            readSnapshot();
        }
        reload();
    }

    public HashMap<String,LevelTuple> getSkillsGroup(SkillsGroup skillsGroup){
        HashMap<String,LevelTuple> result = null;
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
            case CHANGED_SINCE_SNAPSHOT:
                result = changedSinceSnapshot;
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
        OverlayGroup group = new OverlayGroup(client);

        higherOverlay.setGroup(group);
        lowerOverlay.setGroup(group);
        equalOverlay.setGroup(group);
        changedSinceOverlay.setGroup(group);

        higherOverlay.setColor(Color.GREEN);
        lowerOverlay.setColor(Color.RED);
        equalOverlay.setColor(Color.ORANGE);
        changedSinceOverlay.setColor(Color.getHSBColor(329,98,37));

        higherOverlay.setDataProvider(() -> getSkillsGroup(SkillsGroup.HIGHER));
        lowerOverlay.setDataProvider(() -> getSkillsGroup(SkillsGroup.LOWER));
        equalOverlay.setDataProvider(() -> getSkillsGroup(SkillsGroup.EQUAL));
        changedSinceOverlay.setDataProvider(() -> getSkillsGroup(SkillsGroup.CHANGED_SINCE_SNAPSHOT));

        higherOverlay.setVisibilityToggle(() -> config.showHigher());
        lowerOverlay.setVisibilityToggle(() -> config.showLower());
        equalOverlay.setVisibilityToggle(() -> config.showEqual());
        changedSinceOverlay.setVisibilityToggle(() -> config.showChanged());

        higherOverlay.setTitle("Higher Stats:");
        lowerOverlay.setTitle("Lower Stats:");
        equalOverlay.setTitle("Equal Stats:");

        overlayManager.add(higherOverlay);
        overlayManager.add(lowerOverlay);
        overlayManager.add(equalOverlay);
        overlayManager.add(changedSinceOverlay);

        snapshotRepository = new JsonFileSnapshotRepository();
        readSnapshot();
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(higherOverlay);
        overlayManager.remove(lowerOverlay);
        overlayManager.remove(equalOverlay);
        overlayManager.remove(changedSinceOverlay);
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
        takeSnapshot();
        calculateChanged();
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
                int xpDifference = Math.toIntExact(client.getSkillExperience(current) - opponentsSkill.getExperience());
                LevelTuple levelTuple = new LevelTuple(currentLevel, opponentLevel, xpDifference);

                if (currentLevel > opponentLevel) {
                    greaterSkills.put(current.getName(), levelTuple);
                } else if (currentLevel < opponentLevel) {
                    lowerSkills.put(current.getName(), levelTuple);
                } else {
                    equalSkills.put(current.getName(), levelTuple);
                }
            }

            result.getSkill(HiscoreSkill.OVERALL);
            lastRefresh = Instant.now();
        }
    }

    private synchronized void calculateChanged() {
        if(snapshot != null){
            changedSinceSnapshot.clear();
            HiscoreSkill[] allSkills = HiscoreSkill.values();

            for(int i=0;i<allSkills.length;i++){
                HiscoreSkill skill = allSkills[i];
                Skill current = result.getSkill(skill);
                Skill before = snapshot.getSnapshot().getSkill(skill);

                if(current.getExperience() > before.getExperience()){
                    int xpDifference = Math.toIntExact(current.getExperience() - before.getExperience());
                    LevelTuple levelTuple = new LevelTuple(before.getLevel(), current.getLevel(), xpDifference);
                    changedSinceSnapshot.put(skill.getName(), levelTuple);
                }
            }
        }
    }

    private void readSnapshot(){
        changedSinceSnapshot.clear();
        snapshot = snapshotRepository.get(config.playerName());
        snapshotChanged();
    }

    private void snapshotChanged(){
        if(snapshot != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String takenDate = sdf.format(new Date(snapshot.getTimeTaken()*1000L));
            changedSinceOverlay.setTitle("Since "+ takenDate +"");
        }
    }

    private void takeSnapshot(){
        boolean haveResult = result != null;
        boolean saveSnapshot = config.takeSnapshots();
        boolean expired = snapshot == null || snapshot.olderThan(config.snapshotInterval());
        if(saveSnapshot &&  haveResult && expired){
            snapshot = new HiscoreResultSnapshot(result);
            snapshotRepository.save(snapshot, config.playerName());
            snapshotChanged();
        }
    }
}
