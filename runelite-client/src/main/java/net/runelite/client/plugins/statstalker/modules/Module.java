package net.runelite.client.plugins.statstalker.modules;

import net.runelite.client.plugins.statstalker.OverlayGroup;
import net.runelite.client.plugins.statstalker.StatStalkerConfig;
import net.runelite.client.plugins.statstalker.StatsOverlayViewModel;
import net.runelite.http.api.hiscore.HiscoreResult;

import java.util.List;

public interface Module {

    void setResult(HiscoreResult result);

    void refresh();

    void configChanged(StatStalkerConfig config);

    List<StatsOverlayViewModel> load(StatStalkerConfig config, OverlayGroup group);

}
