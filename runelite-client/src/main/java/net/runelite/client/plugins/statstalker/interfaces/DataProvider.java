package net.runelite.client.plugins.statstalker.interfaces;

import java.util.Map;

public interface DataProvider<K,V> {
    Map<K,V> getData();
}
