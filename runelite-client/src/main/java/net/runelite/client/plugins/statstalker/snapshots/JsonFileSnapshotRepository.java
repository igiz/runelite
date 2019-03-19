package net.runelite.client.plugins.statstalker.snapshots;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import net.runelite.client.RuneLite;
import net.runelite.client.plugins.interfaces.Repository;

import java.io.*;
import java.nio.charset.Charset;

public class JsonFileSnapshotRepository implements Repository<HiscoreResultSnapshot> {

    private static final String HI_SCORES_FILE = "hiscore.json";
    private static final String HI_SCORE_SNAPSHOTS_FOLDER = "hiscore_snapshots";

    public HiscoreResultSnapshot get(String playerName) {
        File savedSnapshot = getSnapshotFile(playerName);
        HiscoreResultSnapshot result = null;

        try {
            result = new Gson().fromJson(new JsonReader(new FileReader(savedSnapshot)), HiscoreResultSnapshot.class);
        } catch (FileNotFoundException e) {
            // Ignore we have no snapshot saved for this player.
        }

        return result;
    }

    public void save(HiscoreResultSnapshot snapshot, String playerName){
        String json = new Gson().toJson(snapshot);
        File snapshotToSave = getSnapshotFile(playerName);
        snapshotToSave.getParentFile().mkdirs();

        try (FileOutputStream out = new FileOutputStream(snapshotToSave)) {
            OutputStreamWriter writer = new OutputStreamWriter(out, Charset.forName("UTF-8"));
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File getSnapshotFile(String playerName){
        String fileName = playerName+"_"+HI_SCORES_FILE;
        File directory = new File(RuneLite.RUNELITE_DIR, HI_SCORE_SNAPSHOTS_FOLDER);
        File result = new File(directory, fileName);
        return result;
    }
}