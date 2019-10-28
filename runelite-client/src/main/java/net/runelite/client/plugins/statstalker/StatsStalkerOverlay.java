package net.runelite.client.plugins.statstalker;

import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;

import javax.inject.Inject;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class StatsStalkerOverlay extends Overlay {

    private class FontEnlarger {

        private Font initial;

        public FontEnlarger(Graphics2D graphics, float enlargeBy, boolean bold){
            initial = graphics.getFont();
            Font newFont = initial.deriveFont(initial.getSize() * enlargeBy);
            graphics.setFont(bold? newFont.deriveFont(Font.BOLD): newFont);
        }

        public void reset(Graphics2D graphics){
            graphics.setFont(initial);
        }
    }


    private final SkillIconManager iconManager;
    private final PanelComponent panelComponent = new PanelComponent();
    private StatsStalkerPlugin.Toggle visibilityToggle;

    private StatsStalkerPlugin.DataProvider<String, LevelTuple> dataProvider;


    @Inject
    private StatsStalkerOverlay(StatsStalkerPlugin plugin, SkillIconManager iconManager) {
        this.iconManager = iconManager;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.NONE);
        this.visibilityToggle = () -> !plugin.Visible();
    }

    public void setDataProvider(StatsStalkerPlugin.DataProvider<String, LevelTuple> dataProvider){
        this.dataProvider = dataProvider;
    }

    public void setVisibilityToggle(StatsStalkerPlugin.Toggle visibilityToggle){
        this.visibilityToggle = visibilityToggle;
    }

    @Override
    public Dimension render(Graphics2D graphics) {

        HashMap<String, LevelTuple> data = dataProvider.getData();

        if(!visibilityToggle.isToggled() || data.isEmpty()){
            return null;
        }

        panelComponent.getChildren().clear();

        int x = getBounds().x;
        int y = getBounds().y;

        int size = 40;
        int smallerBy = 10;

        int innerSize = size - smallerBy;
        int pad = smallerBy/2;

        FontEnlarger fontEnlarger = new FontEnlarger(graphics,1.2F, true);
        try{
            for (Map.Entry<String, LevelTuple> entry : data.entrySet()) {
                LevelTuple value = entry.getValue();
                double currentLevel = levelBasedRadius(value.currentLevel );
                double opponentLevel = levelBasedRadius(value.opponentLevel);
                drawLevelArc(graphics, x, y , size, size , currentLevel, Color.GREEN);
                drawLevelArc(graphics, x+pad, y+pad , innerSize, innerSize, opponentLevel, Color.RED);

                int difference = Math.round(value.currentLevel - value.opponentLevel);
                String differenceStr = Integer.toString(difference);
                if(difference > 0){
                    differenceStr = "+"+differenceStr;
                    graphics.setColor(Color.GREEN);
                } else {
                    graphics.setColor(Color.RED);
                }
                graphics.drawString(differenceStr, x+size, y+size);

                drawSkillImage(graphics , x, y , value.skill, size);
                y+=size + 10;
            }
            Dimension result = panelComponent.render(graphics);
            return result;
        } finally {
            fontEnlarger.reset(graphics);
        }
    }

    private double levelBasedRadius(double level){
        double percentage = (level / 99)*100;
        double result = 3.6 * percentage;
        return result;
    }

    private void drawLevelArc(Graphics2D graphics, int x, int y, int w, int h, double radiusEnd, Color color)
    {
        Stroke stroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        graphics.setColor(Color.BLACK);

        graphics.draw(new Arc2D.Double(
                x, y,
                w, h,
                90, radiusEnd,
                Arc2D.PIE));


        graphics.setColor(color);
        graphics.setStroke(stroke);

        graphics.fill(new Arc2D.Double(
                x, y,
                w, h,
                90, radiusEnd,
                Arc2D.PIE));
    }

    private void drawSkillImage(Graphics2D graphics, int x, int y, Skill skill, int size)
    {
        BufferedImage skillImage = iconManager.getSkillImage(skill);

        if (skillImage == null) {
            return;
        }

        graphics.drawImage(
                skillImage,
                x + (size / 2) - (skillImage.getWidth() / 2),
                y + (size / 2) - (skillImage.getHeight() / 2),
                null
        );
    }

}
