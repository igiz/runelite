package net.runelite.client.plugins.statstalker.overlay;

import net.runelite.api.Client;
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
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class StatsStalkerOverlay extends Overlay {

    private final StatComparisonSnapshotService snapshotService;
    private final StatStalkerConfig config;
    private final SkillIconManager skillIconManager;
    private final Client client;
    private final int TextPadding = 20;

    @Inject
    public StatsStalkerOverlay(Client client, StatsStalkerPlugin plugin, StatStalkerConfig config, SkillIconManager skillIconManager) {
        super(plugin);
        this.client = client;
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
        Point mousePosition = getMousePosition();
        ComparisonOrb activeOrb = null;

        for(SkillsGroup group : groupsToRender){
            HashMap<String, LevelComparisonTuple> data = snapshotService.getSnapshot(group);
            addTitles(group, graphics, currentDimension);
            for (Map.Entry<String, LevelComparisonTuple> entry : data.entrySet()) {
                LevelComparisonTuple comparisonTuple = entry.getValue();
                Dimension drawStartPosition = new Dimension(0, currentDimension.height);
                ComparisonOrb orb = new ComparisonOrb(skillIconManager, comparisonTuple, drawStartPosition);
                Dimension orbSize = orb.render(graphics);
                if(checkInBounds(drawStartPosition, orbSize , mousePosition)){
                    activeOrb = orb;
                }
                append(currentDimension, orbSize);
            }
        }

        //Draw tooltip about the active orb
        if(activeOrb != null){
            activeOrb.renderTooltip(graphics, mousePosition);
        }

        return currentDimension;
    }

    private boolean checkInBounds(Dimension startPosition, Dimension size, java.awt.Point position){
        Rectangle graphicBounds = new Rectangle(startPosition.width, startPosition.height, size.width, size.height);
        return graphicBounds.contains(position);
    }

    private Point getMousePosition() {
        //Get the mouse position in the overlay relative to the bounds.
        Rectangle currentPosition = this.getBounds();
        Point mousePosition = new Point(client.getMouseCanvasPosition().getX()-currentPosition.x, client.getMouseCanvasPosition().getY()-currentPosition.y);
        return mousePosition;
    }

    private void addTitles(SkillsGroup group, Graphics2D graphics, Dimension currentDimension){
        //Currently titles only on snapshot comparisons.
        if(group == SkillsGroup.CHANGED_SINCE_SNAPSHOT){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String takenDate = "Changed Since "+sdf.format(new Date(snapshotService.getTimeTaken(SkillsGroup.CHANGED_SINCE_SNAPSHOT)*1000L));
            FontModifier fontModifier = new FontModifier(graphics, Color.WHITE, 1.4F, true);
            graphics.drawString(takenDate, 0, currentDimension.height+TextPadding);
            Dimension textSize = new Dimension(fontModifier.getMetrics().stringWidth(takenDate), fontModifier.getMetrics().getHeight());
            append(currentDimension, textSize);
            fontModifier.reset(graphics);
        }
    }

    private void append(Dimension currentDimension, Dimension newDimension){
        //Going vertical baby!
        currentDimension.width = currentDimension.width > newDimension.width? currentDimension.width : newDimension.width;
        currentDimension.height += newDimension.height;
    }

    private ArrayList<SkillsGroup> getGroupsToDisplay(){

        ArrayList<Pair<SkillsGroup, Function<StatStalkerConfig,Boolean>>> checks = new ArrayList<Pair<SkillsGroup, Function<StatStalkerConfig,Boolean>>>(){
            {
                add(Pair.of(SkillsGroup.HIGHER, (config) -> config.showHigher()));
                add(Pair.of(SkillsGroup.EQUAL, (config) -> config.showEqual()));
                add(Pair.of(SkillsGroup.LOWER, (config) -> config.showLower()));
                add(Pair.of(SkillsGroup.CHANGED_SINCE_SNAPSHOT, (config) -> config.showChanged()));
            }
        };

        ArrayList<SkillsGroup> result = new ArrayList<>();
        for(Pair<SkillsGroup, Function<StatStalkerConfig,Boolean>> checkPair : checks){
            if(checkPair.getRight().apply(config)){
                result.add(checkPair.getLeft());
            }
        }
        return result;
    }
}
