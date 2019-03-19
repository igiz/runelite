package net.runelite.client.plugins.statstalker.snapshots;

import net.runelite.client.plugins.interfaces.Repository;
import net.runelite.http.api.hiscore.HiscoreResult;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HiscoreSnapshotManager {

    private HiscoreResultSnapshot currentSnapshot;

    private Repository<HiscoreResultSnapshot> snapshotRepository;


    public HiscoreSnapshotManager(){
        snapshotRepository = new JsonFileSnapshotRepository();
    }

    public void setPlayer(String player){
        snapshot.getSnapshot().getPlayer() != config.playerName();
    }

    public void setResult(HiscoreResult result){}


    private void readSnapshot(){
        changedSinceSnapshot.clear();
        snapshot = snapshotRepository.get(config.playerName());
        snapshotChanged();
    }

    public HiscoreResult getSnapshot(){

    }

    private void snapshotChanged(){
        if(snapshot != null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String takenDate = sdf.format(new Date(snapshot.getTimeTaken()*1000L));
            changedSinceOverlay.setTitle("Since "+ takenDate +"");
        }
    }

    private void takeSnapshot(){
        boolean haveResult = result != null;
        boolean saveSnapshot = config.takeSnapshots();
        boolean expired = snapshot == null || snapshot.olderThan(config.snapshotInterval());
        if(saveSnapshot &&  haveResult && expired){
            snapshot = new HiscoreResultSnapshot(result);
            snapshotRepository.save(snapshot, config.playerName());
            snapshotChanged();
        }
    }

}
