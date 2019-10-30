package net.runelite.client.plugins.statstalker.overlay;

import java.awt.*;

public class FontModifier {

    private Font initial;
    private Color initialColor;
    private FontMetrics metrics;

    public FontModifier(Graphics2D graphics, float enlargeBy, boolean bold){
        initial = graphics.getFont();
        initialColor = graphics.getColor();
        Font newFont = initial.deriveFont(initial.getSize() * enlargeBy);
        graphics.setFont(bold? newFont.deriveFont(Font.BOLD): newFont);
        metrics = graphics.getFontMetrics(newFont);
    }

    public FontModifier(Graphics2D graphics, Color color, float enlargeBy, boolean bold){
        this(graphics, enlargeBy, bold);
        graphics.setColor(color);
    }

    public void reset(Graphics2D graphics){
        graphics.setFont(initial);
        graphics.setColor(initialColor);
    }

    public FontMetrics getMetrics(){
        return metrics;
    }
}
