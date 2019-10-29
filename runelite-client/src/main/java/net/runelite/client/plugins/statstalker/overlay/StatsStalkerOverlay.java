package net.runelite.client.plugins.statstalker.overlay;

import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.statstalker.LevelComparisonTuple;
import net.runelite.client.plugins.statstalker.SkillsGroup;
import net.runelite.client.plugins.statstalker.StatComparisonSnapshotService;
import net.runelite.client.plugins.statstalker.StatsStalkerPlugin;
import net.runelite.client.plugins.statstalker.config.StatStalkerConfig;
import net.runelite.client.plugins.statstalker.overlay.components.ComparisonOrb;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StatsStalkerOverlay extends Overlay {

    private final StatComparisonSnapshotService snapshotService;
    private final StatStalkerConfig config;
    private final SkillIconManager skillIconManager;
    private final int TextPadding = 20;

    @Inject
    public StatsStalkerOverlay(StatsStalkerPlugin plugin, StatStalkerConfig config, SkillIconManager skillIconManager) {
        super(plugin);

        this.skillIconManager = skillIconManager;
        this.snapshotService = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.LOW);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        ArrayList<SkillsGroup> groupsToRender = getGroupsToDisplay();
        Dimension currentDimension = new Dimension();

        for(SkillsGroup group : groupsToRender){
            HashMap<String, LevelComparisonTuple> data = snapshotService.getSnapshot(group);

            if(group == SkillsGroup.CHANGED_SINCE_SNAPSHOT){
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String takenDate = "Changed Since "+sdf.format(new Date(snapshotService.getTimeTaken(SkillsGroup.CHANGED_SINCE_SNAPSHOT)*1000L));
                FontEnlarger fontEnlarger = new FontEnlarger(graphics, Color.WHITE, 1.4F, true);
                graphics.drawString(takenDate, 0, currentDimension.height+TextPadding);
                Dimension textSize = new Dimension(fontEnlarger.getMetrics().stringWidth(takenDate), fontEnlarger.getMetrics().getHeight());
                append(currentDimension, textSize);
                fontEnlarger.reset(graphics);
            }

            for (Map.Entry<String, LevelComparisonTuple> entry : data.entrySet()) {
                LevelComparisonTuple comparisonTuple = entry.getValue();
                Dimension childDimension = new ComparisonOrb(skillIconManager, comparisonTuple, new Dimension(0, currentDimension.height)).render(graphics);
                append(currentDimension, childDimension);
            }
        }
        return currentDimension;

    }

    private void append(Dimension currentDimension, Dimension newDimension){
        //Going vertical baby!
        currentDimension.width = currentDimension.width > newDimension.width? currentDimension.width : newDimension.width;
        currentDimension.height += newDimension.height;
    }

    private ArrayList<SkillsGroup> getGroupsToDisplay(){

        ArrayList<SkillsGroup> result = new ArrayList<>();

        if(config.showHigher()){
            result.add(SkillsGroup.HIGHER);
        }

        if(config.showEqual()){
            result.add(SkillsGroup.EQUAL);
        }

        if(config.showLower()){
            result.add(SkillsGroup.LOWER);
        }

        if(config.showChanged()){
            result.add(SkillsGroup.CHANGED_SINCE_SNAPSHOT);
        }

        return result;
    }
}
