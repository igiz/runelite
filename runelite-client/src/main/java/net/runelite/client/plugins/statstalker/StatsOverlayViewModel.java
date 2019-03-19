package net.runelite.client.plugins.statstalker;

import net.runelite.client.plugins.statstalker.interfaces.DataProvider;
import net.runelite.client.plugins.statstalker.interfaces.Toggle;
import java.awt.*;

public class StatsOverlayViewModel {
    
    private Toggle enabledToggle;

    private String title;

    private Color color;

    private OverlayGroup group;

    private DataProvider<String, LevelTuple> dataProvider;

    public StatsOverlayViewModel(Toggle enabledToggle, String title, Color color, OverlayGroup group, DataProvider<String, LevelTuple> dataProvider) {
        this.enabledToggle = enabledToggle;
        this.title = title;
        this.color = color;
        this.group = group;
        this.dataProvider = dataProvider;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public boolean isEnabled(){
        return enabledToggle.isOn();
    }

    public DataProvider<String, LevelTuple> getDataProvider(){
        return dataProvider;
    }

    public String getTitle(){
        return title;
    }

    public Color getColor(){
        return color;
    }

    public OverlayGroup getGroup(){
        return group;
    }
}
