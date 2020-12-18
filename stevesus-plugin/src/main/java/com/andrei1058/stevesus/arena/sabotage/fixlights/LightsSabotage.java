package com.andrei1058.stevesus.arena.sabotage.fixlights;

import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameListener;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.sabotage.GenericWarning;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageProvider;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.event.GameSabotageActivateEvent;
import com.andrei1058.stevesus.api.event.GameSabotageDeactivateEvent;
import com.andrei1058.stevesus.api.glow.GlowColor;
import com.andrei1058.stevesus.api.glow.GlowingBox;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.server.PlayerCoolDown;
import com.andrei1058.stevesus.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.UUID;

public class LightsSabotage extends SabotageBase {

    private boolean active = false;
    private final Arena arena;
    private final GenericWarning warning;
    private final GlowingBox glowingBox;
    private final LinkedList<UUID> openGUI = new LinkedList<>();
    private FixLightsGUI gui;

    public LightsSabotage(Arena arena, Location location) {
        this.arena = arena;
        this.warning = new GenericWarning(arena, 1, LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, LightsSabotageProvider.NAME_PATH));
        this.glowingBox = new GlowingBox(location.add(0.5, 0, 0.5), 2, GlowColor.RED);


        arena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerInteractEntity(Arena arena, Player player, Entity entity) {
                if (entity.getType() != EntityType.MAGMA_CUBE) return;
                if (!entity.equals(glowingBox.getMagmaCube())) return;
                PlayerCoolDown coolDown = PlayerCoolDown.getOrCreatePlayerData(player);
                if (coolDown.hasCoolDown("magmaCube")) return;
                coolDown.updateCoolDown("magmaCube", 1);
                Team team = arena.getPlayerTeam(player);
                if (team != null && team.getIdentifier().endsWith("-ghost")){
                    return;
                }

                Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                if (gui == null){
                    gui = new FixLightsGUI(lang, LightsSabotage.this, player);
                }
                openGUI.add(player.getUniqueId());
                gui.open(player);
            }

            @Override
            public void onInventoryClose(Arena arena, Player player, Inventory inventory) {
                openGUI.remove(player.getUniqueId());
            }

            @Override
            public void onMeetingStageChange(Arena arena, MeetingStage oldStage, MeetingStage newStage) {
                // remove dark, re-add dark on end
            }

            @Override
            public void onPlayerLeave(Arena arena, Player player, boolean spectator) {
                warning.removePlayer(player);
            }
        });
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void tryDeactivate() {
        active = false;
        warning.restore();
        getArena().getPlayers().forEach(player -> player.sendTitle(" ", LanguageManager.getINSTANCE().getMsg(player, LightsSabotageProvider.FIXED_SUBTITLE), 0, 40, 0));
        arena.tryEnableTaskIndicators();
        Bukkit.getPluginManager().callEvent(new GameSabotageDeactivateEvent(getArena(), this, false));
        for (Player playing : arena.getPlayers()) {
            glowingBox.stopGlowing(playing);
        }
        for (UUID uuid : getOpenGUI()){
            Player player = Bukkit.getPlayer(uuid);
            if (player != null){
                player.closeInventory();
            }
        }
        for (Player player : arena.getPlayers()){
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }

    public Arena getArena() {
        return arena;
    }

    @Override
    public void activate(@Nullable Player player) {
        warning.setBarName(LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, LightsSabotageProvider.NAME_PATH));
        warning.sendBar();
        active = true;
        arena.interruptTasks();
        arena.disableTaskIndicators();
        Bukkit.getPluginManager().callEvent(new GameSabotageActivateEvent(getArena(), this));
        for (Player playing : arena.getPlayers()) {
            glowingBox.startGlowing(playing);
        }
        for (Team team : arena.getGameTeams()){
            if (!team.getIdentifier().endsWith("-ghost")){
                for (Player member : team.getMembers()){
                    glowingBox.startGlowing(member);
                    if (team.isInnocent()){
                        member.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 5, false));
                    }
                }
            }
        }
    }

    public LinkedList<UUID> getOpenGUI() {
        return openGUI;
    }

    @Override
    public @NotNull SabotageProvider getProvider() {
        return LightsSabotageProvider.getInstance();
    }
}
