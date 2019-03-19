package net.runelite.client.plugins.statstalker;

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
import java.util.Map;

public class StatsStalkerOverlay extends Overlay {

    private @Inject TooltipManager tooltipManager;

    private final StatsOverlayViewModel viewModel;

    private final PanelComponent panelComponent = new PanelComponent();

    public StatsStalkerOverlay(StatsOverlayViewModel viewModel){
        this.viewModel = viewModel;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.NONE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        Map<String, LevelTuple> data = viewModel.getDataProvider().getData();

        if(!viewModel.isEnabled() || data.isEmpty()){
            return null;
        }

        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(LineComponent.builder()
                .left(viewModel.getTitle())
                .build());

        String[] tooltips = new String[data.size()+2];
        tooltips[0]= "        XP Differences";
        tooltips[1]= "--------------------------";
        int i=2;
        for (Map.Entry<String, LevelTuple> entry : data.entrySet()) {
            String skill = entry.getKey();
            LevelTuple value = entry.getValue();

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(skill)
                    .right(value.getCurrentLevel() + " | " + value.getOpponentLevel())
                    .rightColor(viewModel.getColor())
                    .build());

            tooltips[i] = skill+" : " + NumberFormat.getInstance().format(value.getXpDifference());
            i++;
        }

        Dimension result = panelComponent.render(graphics);

        if (viewModel.getGroup().showTooltips(this)){
            final String tooltip = String.join("</br>", tooltips);
            tooltipManager.add(new Tooltip(tooltip));
        }

        return result;
    }

}
