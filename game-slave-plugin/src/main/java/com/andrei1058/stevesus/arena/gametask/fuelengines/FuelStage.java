package com.andrei1058.stevesus.arena.gametask.fuelengines;

import com.andrei1058.hologramapi.Hologram;
import com.andrei1058.hologramapi.HologramPage;
import com.andrei1058.hologramapi.content.LineTextContent;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.WeakHashMap;

public class FuelStage {

    private GlowingBox storageGlowing;
    private GlowingBox engineGlowing;
    private Hologram storageHologram;
    private Hologram engineHologram;
    private final WeakHashMap<UUID, Long> nextAllowed = new WeakHashMap<>();

    public FuelStage(@Nullable Location storage, @Nullable Location reactor) {
        if (storage != null) {
            storageGlowing = new GlowingBox(storage.add(0.5, 0, 0.5), 2, GlowColor.DARK_AQUA);
        }
        if (reactor != null) {
            engineGlowing = new GlowingBox(reactor.add(0.5, 0, 0.5), 2, GlowColor.DARK_AQUA);
        }
    }

    @Nullable
    public GlowingBox getStorageGlowing() {
        return storageGlowing;
    }

    @Nullable
    public GlowingBox getEngineGlowing() {
        return engineGlowing;
    }

    public Hologram getStorageHologram() {
        return storageHologram;
    }

    public Hologram getEngineHologram() {
        return engineHologram;
    }

    public void initHolograms(GameTask task) {
        if (getStorageGlowing() != null) {
            storageHologram = new Hologram(getStorageGlowing().getMagmaCube().getLocation().add(0, 0.5, 0), 1);
            HologramPage page1 = storageHologram.getPage(0);
            assert page1 != null;
            page1.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.GAME_TASK_PATH_.toString() + task.getHandler().getIdentifier() + "-" + task.getLocalName() + "holo1")));
        }
        if (getEngineGlowing() != null) {
            engineHologram = new Hologram(getEngineGlowing().getMagmaCube().getLocation().add(0, 0.5, 0), 1);
            HologramPage page2 = engineHologram.getPage(0);
            assert page2 != null;
            page2.setLineContent(0, new LineTextContent(s -> LanguageManager.getINSTANCE().getMsg(s, Message.GAME_TASK_PATH_.toString() + task.getHandler().getIdentifier() + "-" + task.getLocalName() + "holo2")));
        }
    }

    public void onInteract(Player player, boolean halfDone, FuelEnginesTask task, Arena arena, Entity entity) {
        if (!arena.isTasksAllowedATM()) return;
        if (!GlowingManager.isGlowing(entity, player)) {
            return;
        }

        if (nextAllowed.containsKey(player.getUniqueId())) {
            if (nextAllowed.get(player.getUniqueId()) > System.currentTimeMillis()) {
                return;
            }
            nextAllowed.replace(player.getUniqueId(), System.currentTimeMillis() + 1000L);
        } else {
            nextAllowed.put(player.getUniqueId(), System.currentTimeMillis() + 1000L);
        }
        Locale lang = LanguageManager.getINSTANCE().getLocale(player.getUniqueId());
        StorageGUI gui = new StorageGUI(lang, task, arena);
        gui.open(player);
    }
}
