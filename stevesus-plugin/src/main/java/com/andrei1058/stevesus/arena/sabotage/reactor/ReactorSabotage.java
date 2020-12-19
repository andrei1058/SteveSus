package com.andrei1058.stevesus.arena.sabotage.reactor;

import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.sabotage.GenericWarning;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageProvider;
import com.andrei1058.stevesus.api.arena.sabotage.TimedSabotage;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.event.GameSabotageActivateEvent;
import com.andrei1058.stevesus.api.event.GameSabotageDeactivateEvent;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class ReactorSabotage extends SabotageBase implements TimedSabotage {

    private final Arena arena;
    private final GenericWarning warning;
    private int deadLineSeconds;
    private boolean active = false;
    private final GlowingBox loc1;
    private final GlowingBox loc2;
    private final HashMap<Player, ReactorGUI> openGUIs = new HashMap<>();

    public ReactorSabotage(Arena arena, int deadLineSeconds, Location loc1, Location loc2) {
        this.arena = arena;
        this.deadLineSeconds = deadLineSeconds;
        this.loc1 = new GlowingBox(loc1.add(0.5,0,0.5), 2, GlowColor.RED);
        this.loc2 = new GlowingBox(loc2.add(0.5,0,0.5), 2, GlowColor.RED);
        this.warning = new GenericWarning(arena, deadLineSeconds, LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, ReactorSabotageProvider.NAME_PATH));

        arena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerLeave(Arena arena, Player player, boolean spectator) {
                warning.removePlayer(player);
            }

            @Override
            public void onPlayerToSpectator(Arena arena, Player player) {
                warning.removePlayer(player);
            }

            @Override
            public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
                tryOpen(player, entity);
            }

            @Override
            public void onEntityPunch(Arena arena, Player player, Entity entity) {
                tryOpen(player, entity);
            }

            @Override
            public void onMeetingStageChange(Arena arena, MeetingStage oldStage, MeetingStage newStage) {
                if (oldStage == MeetingStage.NO_MEETING) {
                    deactivate(true);
                }
            }

            @Override
            public void onInventoryClose(Arena arena, Player player, Inventory inventory) {
                if (openGUIs.containsKey(player)) {
                    ReactorGUI gui = openGUIs.remove(player);
                    if (gui != null && gui.getTaskId() != -1) {
                        Bukkit.getScheduler().cancelTask(gui.getTaskId());
                    }
                }
            }
        });
    }

    private void tryOpen(Player player, Entity entity) {
        if (!isActive()) return;
        if (loc1 == null || loc2 == null) return;
        if (entity.getType() != EntityType.MAGMA_CUBE) return;
        Team playerTeam = arena.getPlayerTeam(player);
        if (playerTeam == null || playerTeam.getIdentifier().endsWith("-ghost")) return;
        if (entity.equals(loc1.getMagmaCube())) {
            Locale lang = LanguageManager.getINSTANCE().getLocale(player);
            ReactorGUI gui = new ReactorGUI(lang, true, false);
            gui.open(player);
            openGUIs.put(player, gui);
            checkSecondUser();
        } else if (entity.equals(loc2.getMagmaCube())) {
            Locale lang = LanguageManager.getINSTANCE().getLocale(player);
            ReactorGUI gui = new ReactorGUI(lang, false, false);
            gui.open(player);
            openGUIs.put(player, gui);
            checkSecondUser();
        }
    }

    private void checkSecondUser() {
        boolean first = false;
        boolean second = false;

        for (ReactorGUI gui : openGUIs.values()) {
            if (gui.isFirst()) {
                first = true;
            } else {
                second = true;
            }
            if (first && second) {
                break;
            }
        }


        if (first && second) {
            SteveSus.newChain().delay(10).sync(() -> {
                if (getCountDown() > 0) {
                    for (Player player : new ArrayList<>(openGUIs.keySet())) {
                        Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                        ReactorGUI gui = new ReactorGUI(lang, false, true);
                        player.closeInventory();
                        gui.open(player);
                        openGUIs.put(player, gui);
                    }
                    deactivate(false);
                    SteveSus.newChain().delay(13).sync(()-> {
                        for (Player player : new ArrayList<>(openGUIs.keySet())) {
                            player.closeInventory();
                        }
                    }).execute();
                } else {
                    for (Player player : new ArrayList<>(openGUIs.keySet())) {
                        player.closeInventory();
                    }
                }
            }).execute();
        }
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void activate(@Nullable Player player) {
        warning.setBarName(LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, ReactorSabotageProvider.NAME_PATH));
        warning.sendBar();
        active = true;
        //arena.interruptTasks();
        arena.disableTaskIndicators();
        Bukkit.getPluginManager().callEvent(new GameSabotageActivateEvent(getArena(), this));
        for (Player player1 : arena.getPlayers()){
            loc1.startGlowing(player1);
            loc2.startGlowing(player1);
        }
    }

    public void deactivate(boolean forced) {
        active = false;
        warning.restore();
        this.deadLineSeconds = warning.getOriginalSeconds();
        arena.tryEnableTaskIndicators();
        Bukkit.getPluginManager().callEvent(new GameSabotageDeactivateEvent(getArena(), this, forced));
        if (!forced) {
            // title
            for (Player player : getArena().getPlayers()) {
                player.sendTitle(" ", LanguageManager.getINSTANCE().getMsg(player, ReactorSabotageProvider.FIXED_SUBTITLE), 0, 40, 0);
                loc1.stopGlowing(player);
                loc2.stopGlowing(player);
            }
        }
        if (forced) {
            for (ReactorGUI gui : openGUIs.values()) {
                if (gui.getTaskId() != -1) {
                    Bukkit.getScheduler().cancelTask(gui.getTaskId());
                }
            }
            openGUIs.clear();
        }
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public @NotNull SabotageProvider getProvider() {
        return ReactorSabotageProvider.getInstance();
    }

    @Override
    public int getCountDown() {
        return deadLineSeconds;
    }

    @Override
    public void doTick() {
        if (isActive()) {
            if (deadLineSeconds == 0) {
                getArena().removeSabotage(this);
                warning.refreshWarning(0);
                for (Player player : getArena().getPlayers()){
                    player.closeInventory();
                }
                getArena().defeatBySabotage(Message.DEFEAT_REASON_PATH_.toString() + getProvider().getUniqueIdentifier());
            } else {
                warning.refreshWarning(--deadLineSeconds);
            }
        }
    }
}
