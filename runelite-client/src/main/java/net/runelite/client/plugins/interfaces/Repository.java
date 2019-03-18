package net.runelite.client.plugins.interfaces;

public interface Repository<T>  {

    void save(T item, String path);

    T get(String path);

}
