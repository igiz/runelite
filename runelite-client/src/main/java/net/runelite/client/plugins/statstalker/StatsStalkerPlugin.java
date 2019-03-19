package net.runelite.client.plugins.statstalker;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.ExperienceChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.statstalker.modules.Module;
import net.runelite.client.plugins.statstalker.modules.comparison.HiScoreComparisonModule;
import net.runelite.client.plugins.statstalker.modules.snapshots.HiscoreSnapshotModule;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.hiscore.HiscoreClient;
import net.runelite.http.api.hiscore.HiscoreEndpoint;
import net.runelite.http.api.hiscore.HiscoreResult;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@PluginDescriptor(
        name = "Stats Stalker",
        description = "Stalk other players statistics compared to yours.",
        enabledByDefault = false
)
public class StatsStalkerPlugin extends Plugin {

    private final HiscoreClient hiscoreClient = new HiscoreClient();

    private final List<Module> modules = new ArrayList<>(
            new ArrayList(Arrays.asList(new Module[]{
                    new HiScoreComparisonModule(),
                    new HiscoreSnapshotModule()
            }))
    );

    private final ArrayList<StatsStalkerOverlay> overlays = new ArrayList<>();

    private Instant lastReload = Instant.MIN;

    @Inject
    private StatStalkerConfig config;

    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Provides
    StatStalkerConfig getConfig(ConfigManager configManager)
    {
        return configManager.getConfig(StatStalkerConfig.class);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        for(Module module : modules){
            module.configChanged(config);
        }

        reload();
    }

    @Override
    protected void startUp() throws Exception
    {
        OverlayGroup group = new OverlayGroup(client);
        ArrayList<StatsOverlayViewModel> viewModels = new ArrayList<>();

        for(Module module : modules){
            viewModels.addAll(module.load(config,group));
        }

        for (StatsOverlayViewModel viewModel : viewModels) {
            StatsStalkerOverlay overlay = new StatsStalkerOverlay(viewModel);
            overlays.add(overlay);
            overlayManager.add(overlay);
        }
    }

    @Override
    protected void shutDown() throws Exception
    {
        for (StatsStalkerOverlay overlay : overlays) {
            overlayManager.remove(overlay);
        }
    }

    @Subscribe
    public void onExperienceChanged(ExperienceChanged event) {
        refresh();
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        Duration timeSinceInfobox = Duration.between(lastReload, Instant.now());
        Duration statTimeout = Duration.ofMinutes(config.refreshInterval());
        if (timeSinceInfobox.compareTo(statTimeout) >= 0) {
            reload();
        }
    }

    private void reload() {

        CompletableFuture.supplyAsync(() -> {
            try {
                HiscoreResult hiScore = hiscoreClient.lookup(config.playerName(), HiscoreEndpoint.NORMAL);
                return hiScore;
            } catch (IOException e) {
                return null;
            }
        })
                .thenAccept(hiscoreResult -> setResult(hiscoreResult))
                .exceptionally(throwable ->
                        {
                            throwable.printStackTrace();
                            return null;
                        })
                .thenRun(() -> {
                    lastReload = Instant.now();
                    refresh();
                });
    }

    private synchronized void setResult(HiscoreResult result){
        for(Module module : modules){
            module.setResult(result);
        }
    }

    private synchronized void refresh() {
        for(Module module : modules){
            module.refresh();
        }
    }
}
