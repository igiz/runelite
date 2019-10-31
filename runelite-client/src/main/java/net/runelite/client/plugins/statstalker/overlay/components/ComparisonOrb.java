package net.runelite.client.plugins.statstalker.overlay.components;

import net.runelite.api.Skill;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.statstalker.LevelComparisonTuple;
import net.runelite.client.plugins.statstalker.overlay.FontModifier;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;

public class ComparisonOrb extends Overlay {

    // Constants
    private static final int OuterOrbSize = 40;
    private static final int InnerOrbSmallerBy = 10;
    private static final int Padding = 10;

    private final SkillIconManager iconManager;
    private final LevelComparisonTuple comparisonTuple;
    private final Dimension startPosition;
    private final Dimension size;

    public ComparisonOrb(SkillIconManager iconManager, LevelComparisonTuple comparisonTuple, Dimension startPosition){
        this.iconManager = iconManager;
        this.comparisonTuple = comparisonTuple;
        this.startPosition = startPosition;
        this.size = new Dimension(OuterOrbSize + Padding + 25, OuterOrbSize + Padding);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        int innerOrbSize = OuterOrbSize - InnerOrbSmallerBy;
        int pad = InnerOrbSmallerBy / 2;

        double currentLevel = levelBasedRadius(comparisonTuple.currentLevel);
        double opponentLevel = levelBasedRadius(comparisonTuple.opponentLevel);

        int x = startPosition.width + (Padding / 2);
        int y = startPosition.height + (Padding / 2);

        //Draw the various graphic components
        drawLevelArc(graphics, x, y, OuterOrbSize, OuterOrbSize, currentLevel, Color.GREEN);
        drawLevelArc(graphics, x + pad, y + pad, innerOrbSize, innerOrbSize, opponentLevel, Color.RED);
        drawSkillImage(graphics, x, y, comparisonTuple.skill, OuterOrbSize);
        drawLevelString(graphics, x, y);

        return size;
    }

    public void renderTooltip(Graphics2D graphics, java.awt.Point mousePosition) {
        PanelComponent tooltip = new PanelComponent();
        tooltip.setBackgroundColor(new Color(0,0,0,0));
        tooltip.setPreferredLocation(new Point(mousePosition.x, mousePosition.y + 10));
        tooltip.setBackgroundColor(Color.BLACK);

        tooltip.getChildren().add(LineComponent.builder()
                .left("Level:\n" + comparisonTuple.opponentLevel)
                .leftColor(Color.RED)
                .right("Level:\n" + comparisonTuple.currentLevel)
                .rightColor(Color.GREEN)
                .build());

        DecimalFormat decimalFormat = new DecimalFormat("###,###,###");
        tooltip.getChildren().add(LineComponent.builder()
                .left("XP Difference:\n" + decimalFormat.format(comparisonTuple.xpDifference))
                .leftColor(Color.WHITE)
                .build()
        );

        FontModifier modifier = new FontModifier(graphics, 1.2F, true);
        tooltip.render(graphics);
        modifier.reset(graphics);
    }

    private double levelBasedRadius(double level){
        double percentage = (level / 99)*100;
        double result = 3.6 * percentage;
        return result;
    }


    private void drawLevelString(Graphics2D graphics, int x, int y){
        int difference = Math.round(comparisonTuple.currentLevel - comparisonTuple.opponentLevel);
        String differenceStr = Integer.toString(difference);

        if (difference > 0) {
            differenceStr = "+" + differenceStr;
            graphics.setColor(Color.GREEN);
        } else if (difference == 0) {
            graphics.setColor(Color.ORANGE);
        } else {
            graphics.setColor(Color.RED);
        }

        FontModifier fontModifier = new FontModifier(graphics,1.2F, true);
        graphics.drawString(differenceStr, x + OuterOrbSize, y + OuterOrbSize);
        fontModifier.reset(graphics);
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
        graphics.drawImage(
                skillImage,
                x + (size / 2) - (skillImage.getWidth() / 2),
                y + (size / 2) - (skillImage.getHeight() / 2),
                null
        );
    }
}
