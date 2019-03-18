package net.runelite.client.plugins.statstalker;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;

import java.awt.*;
import java.util.ArrayList;

public class OverlayGroup {

    private ArrayList<Overlay> group;
    private Client client;

    public OverlayGroup(Client client){
        this.client = client;
        this.group = new ArrayList<>();
    }

    public void add(Overlay item){
        group.add(item);
    }

    public void remove(Overlay item){
        group.remove(item);
    }

    public boolean showTooltips(Overlay overlay){
        Rectangle rectangle = overlay.getBounds();
        Point mouse = getMousePosition();
        return rectangle.contains(mouse);
    }

    private Point getMousePosition(){
        Point mouse = new Point(client.getMouseCanvasPosition().getX(),client.getMouseCanvasPosition().getY());
        return mouse;
    }

}
