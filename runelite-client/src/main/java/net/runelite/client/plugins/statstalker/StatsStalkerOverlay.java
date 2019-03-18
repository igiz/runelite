package net.runelite.client.plugins.statstalker;

import net.runelite.api.Client;
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

    private final Client client;
    private final TooltipManager tooltipManager;
    private final PanelComponent panelComponent = new PanelComponent();
    private OverlayGroup group;
    private StatsStalkerPlugin.Toggle visibilityToggle;
    private String title;
    private Color color;

    private StatsStalkerPlugin.DataProvider<String, StatsStalkerPlugin.LevelTuple> dataProvider;

    @Inject
    private StatsStalkerOverlay(TooltipManager tooltipManager, StatsStalkerPlugin plugin, Client client) {
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.NONE);
        this.tooltipManager = tooltipManager;
        this.client = client;
        this.title = "";
        this.color = Color.WHITE;
        this.visibilityToggle = () -> !plugin.Visible();
    }

    public void setGroup(OverlayGroup group){
        this.group = group;
        this.group.add(this);
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public void setDataProvider(StatsStalkerPlugin.DataProvider<String, StatsStalkerPlugin.LevelTuple> dataProvider){
        this.dataProvider = dataProvider;
    }

    public void setVisibilityToggle(StatsStalkerPlugin.Toggle visibilityToggle){
        this.visibilityToggle = visibilityToggle;
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        HashMap<String, StatsStalkerPlugin.LevelTuple> data = dataProvider.getData();

        if(!visibilityToggle.isToggled() || data.isEmpty()){
            return null;
        }

        panelComponent.getChildren().clear();

        if(!title.isEmpty()){
            panelComponent.getChildren().add(LineComponent.builder()
                    .left(title)
                    .build());
        }

        String[] tooltips = new String[data.size()+2];
        tooltips[0]= "        XP Differences";
        tooltips[1]= "--------------------------";
        int i=2;
        for (Map.Entry<String, StatsStalkerPlugin.LevelTuple> entry : data.entrySet()) {
            String skill = entry.getKey();
            StatsStalkerPlugin.LevelTuple value = entry.getValue();

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(skill)
                    .right(value.currentLevel + " | " + value.opponentLevel)
                    .rightColor(color)
                    .build());

            tooltips[i] = skill+" : " + NumberFormat.getInstance().format(value.xpDifference);
            i++;
        }

        Dimension result = panelComponent.render(graphics);

        if (showTooltips()){
            final String tooltip = String.join("</br>", tooltips);
            tooltipManager.add(new Tooltip(tooltip));
        }

        return result;
    }

    private boolean showTooltips(){
        if(group != null){
            return group.showTooltips(this);
        } else {
            final Rectangle intersectionRectangle = new Rectangle(panelComponent.getBounds());
            final Point mouse = new Point(client.getMouseCanvasPosition().getX(),client.getMouseCanvasPosition().getY());
            return intersectionRectangle.contains(mouse);
        }
    }

}
