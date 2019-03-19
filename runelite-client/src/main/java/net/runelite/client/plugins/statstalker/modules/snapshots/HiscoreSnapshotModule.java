package net.runelite.client.plugins.statstalker.modules.snapshots;

import net.runelite.client.plugins.interfaces.Repository;
import net.runelite.client.plugins.statstalker.*;
import net.runelite.client.plugins.statstalker.interfaces.Toggle;
import net.runelite.client.plugins.statstalker.modules.Module;
import net.runelite.http.api.hiscore.HiscoreResult;
import net.runelite.http.api.hiscore.HiscoreSkill;
import net.runelite.http.api.hiscore.Skill;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class HiscoreSnapshotModule implements Module {

    private final HashMap<String,LevelTuple> changed = new HashMap<>();
    private final Repository<HiscoreResultSnapshot> snapshotRepository = new JsonFileSnapshotRepository();

    private StatsOverlayViewModel viewModel;
    private HiscoreResultSnapshot snapshot;
    private HiscoreResult result;

    private boolean saveSnapshots;
    private int snapshotInterval;

    public void setResult(HiscoreResult result){
        this.result = result;
        writeSnapshot(result);
    }

    @Override
    public void refresh() {
        changed.clear();
        if(snapshot != null && result != null) {
            HiscoreResult saved = snapshot.getSnapshot();
            HiscoreSkill[] allSkills = HiscoreSkill.values();

            for (int i = 0; i < allSkills.length; i++) {
                HiscoreSkill skill = allSkills[i];
                Skill current = result.getSkill(skill);
                Skill before = saved.getSkill(skill);

                if (current.getExperience() > before.getExperience()) {
                    int xpDifference = Math.toIntExact(current.getExperience() - before.getExperience());
                    LevelTuple levelTuple = new LevelTuple(before.getLevel(), current.getLevel(), xpDifference);
                    changed.put(skill.getName(), levelTuple);
                }
            }
        }
    }

    @Override
    public void configChanged(StatStalkerConfig config) {
        init(config);
    }

    @Override
    public List<StatsOverlayViewModel> load(StatStalkerConfig config, OverlayGroup group) {
        viewModel = new StatsOverlayViewModel(() -> config.showChanged(), "Changed Since Stats:", Color.getHSBColor(329, 98, 37), group, () -> changed);
        List<StatsOverlayViewModel> result = new ArrayList(Arrays.asList(new StatsOverlayViewModel[]{
                viewModel
        }));
        init(config);

        return result;
    }

    private void init(StatStalkerConfig config){
        saveSnapshots = config.takeSnapshots();
        snapshotInterval = config.snapshotInterval();
        readSnapshot(config.playerName());
    }

    private void readSnapshot(String name){
        snapshot = snapshotRepository.get(name);
        snapshotChanged();
    }

    private void snapshotChanged(){
        if(snapshot != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String takenDate = sdf.format(new Date(snapshot.getTimeTaken()*1000L));
            viewModel.setTitle("Since "+ takenDate +"");
        }
    }

    private void writeSnapshot(HiscoreResult result){
        boolean expired = snapshot == null || snapshot.olderThan(snapshotInterval);
        if(saveSnapshots && expired){
            snapshot = new HiscoreResultSnapshot(result);
            snapshotRepository.save(snapshot, result.getPlayer());
            snapshotChanged();
        }
    }

}
