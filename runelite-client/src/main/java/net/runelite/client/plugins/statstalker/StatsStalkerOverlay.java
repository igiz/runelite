package net.runelite.client.plugins.statstalker;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

import javax.inject.Inject;
import java.awt.*;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class StatsStalkerOverlay extends Overlay {

    private final StatStalkerConfig config;
    private final StatsStalkerPlugin plugin;
    private final Client client;

    private final TooltipManager tooltipManager;
    private final PanelComponent panelComponent = new PanelComponent();
    private String title;
    private Color color;

    private StatsStalkerPlugin.DataProvider<net.runelite.api.Skill, StatsStalkerPlugin.LevelTuple> dataProvider;

    @Inject
    private StatsStalkerOverlay(TooltipManager tooltipManager, StatsStalkerPlugin plugin, StatStalkerConfig config, Client client) {
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.NONE);
        this.tooltipManager = tooltipManager;
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        this.title = "";
        this.color = Color.WHITE;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public void setDataProvider(StatsStalkerPlugin.DataProvider<net.runelite.api.Skill, StatsStalkerPlugin.LevelTuple> dataProvider){
        this.dataProvider = dataProvider;
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        HashMap<net.runelite.api.Skill, StatsStalkerPlugin.LevelTuple> data = dataProvider.getData();

        if(!plugin.Visible() || data.isEmpty()){
            return null;
        }

        panelComponent.getChildren().clear();

        if(!title.isEmpty()){
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(title)
                    .build());
        }

        String[] tooltips = new String[data.size()+1];
        tooltips[0]= "XP Differences:";

        int i=1;
        for (Map.Entry<net.runelite.api.Skill, StatsStalkerPlugin.LevelTuple> entry : data.entrySet()) {
            net.runelite.api.Skill skill = entry.getKey();
            StatsStalkerPlugin.LevelTuple value = entry.getValue();

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(skill.getName())
                    .right(value.currentLevel + " | " + value.opponentLevel)
                    .rightColor(color)
                    .build());

            tooltips[i] = skill.getName()+" : " + NumberFormat.getInstance().format(value.xpDifference);
            i++;
        }

        final Rectangle intersectionRectangle = new Rectangle(panelComponent.getBounds());
        final Point mouse = new Point(client.getMouseCanvasPosition().getX(), client.getMouseCanvasPosition().getY());

        if (intersectionRectangle.contains(mouse)){
            final String tooltip = String.join("</br>", tooltips);
            tooltipManager.add(new Tooltip(tooltip));
        }

        return panelComponent.render(graphics);
    }

}
