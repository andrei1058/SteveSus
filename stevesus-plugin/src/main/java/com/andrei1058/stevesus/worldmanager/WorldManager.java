package com.andrei1058.stevesus.worldmanager;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.world.WorldAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

public class WorldManager implements com.andrei1058.stevesus.api.world.WorldManager {

    private static WorldManager INSTANCE;

    private WorldAdapter worldAdapter = new InternalWorldAdapter();

    private WorldManager() {
    }

    public static void onLoad() {
        if (INSTANCE != null) return;
        INSTANCE = new WorldManager();
        INSTANCE.worldAdapter.onAdapterInitialize();
        SteveSus.getInstance().getLogger().info("Initializing world adapter: " + INSTANCE.worldAdapter.getAdapterName());
    }

    public static WorldManager getINSTANCE() {
        return INSTANCE;
    }

    public WorldAdapter getWorldAdapter() {
        return worldAdapter;
    }

    @Override
    public boolean setWorldAdapter(@Nullable WorldAdapter worldAdapter) {
        //todo check if there are loaded arenas
        //todo check if there are active setup sessions
        //todo else set adapter

        // If null set to internal adapter
        INSTANCE.worldAdapter = worldAdapter == null ? new InternalWorldAdapter() : worldAdapter;
        INSTANCE.worldAdapter.onAdapterInitialize();
        SteveSus.getInstance().getLogger().info("Initializing world adapter: " + INSTANCE.worldAdapter.getAdapterName());
        return true;
    }
}
