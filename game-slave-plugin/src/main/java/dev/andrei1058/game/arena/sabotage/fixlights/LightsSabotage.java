package dev.andrei1058.game.arena.sabotage.fixlights;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.api.arena.GameListener;
import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import dev.andrei1058.game.api.arena.sabotage.GenericWarning;
import dev.andrei1058.game.api.arena.sabotage.SabotageBase;
import dev.andrei1058.game.api.arena.sabotage.SabotageProvider;
import dev.andrei1058.game.api.arena.team.Team;
import dev.andrei1058.game.api.event.GameSabotageActivateEvent;
import dev.andrei1058.game.api.event.GameSabotageDeactivateEvent;
import dev.andrei1058.game.api.glow.GlowColor;
import dev.andrei1058.game.api.glow.GlowingBox;
import dev.andrei1058.game.api.locale.Locale;
import dev.andrei1058.game.api.server.PlayerCoolDown;
import dev.andrei1058.game.language.LanguageManager;
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
    private final GameArena gameArena;
    private final GenericWarning warning;
    private final GlowingBox glowingBox;
    private final LinkedList<UUID> openGUI = new LinkedList<>();
    private FixLightsGUI gui;

    public LightsSabotage(GameArena gameArena, Location location) {
        this.gameArena = gameArena;
        this.warning = new GenericWarning(gameArena, 1, LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, LightsSabotageProvider.NAME_PATH));
        this.glowingBox = new GlowingBox(location.add(0.5, 0, 0.5), 2, GlowColor.RED);


        gameArena.registerGameListener(new GameListener() {
            @Override
            public void onPlayerInteractEntity(GameArena gameArena, Player player, Entity entity) {
                if (entity.getType() != EntityType.MAGMA_CUBE) return;
                if (!isActive()) return;
                if (!entity.equals(glowingBox.getMagmaCube())) return;
                if (gameArena.getCamHandler() != null && gameArena.getCamHandler().isOnCam(player, gameArena)) return;
                PlayerCoolDown coolDown = PlayerCoolDown.getOrCreatePlayerData(player);
                if (coolDown.hasCoolDown("magmaCube")) return;
                coolDown.updateCoolDown("magmaCube", 1);
                Team team = gameArena.getPlayerTeam(player);
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
            public void onInventoryClose(GameArena gameArena, Player player, Inventory inventory) {
                openGUI.remove(player.getUniqueId());
            }

            @Override
            public void onMeetingStageChange(GameArena gameArena, MeetingStage oldStage, MeetingStage newStage) {
                if (!isActive()) return;
                if (newStage == MeetingStage.NO_MEETING){
                    for (Team team : gameArena.getGameTeams()){
                        if (!team.getIdentifier().endsWith("-ghost")){
                            for (Player member : team.getMembers()){
                                glowingBox.startGlowing(member);
                                if (team.isInnocent()){
                                    member.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 5, false));
                                }
                            }
                        }
                    }
                } else {
                    for (Player inGame : gameArena.getPlayers()){
                        inGame.removePotionEffect(PotionEffectType.BLINDNESS);
                    }
                }
            }

            @Override
            public void onPlayerLeave(GameArena gameArena, Player player, boolean spectator) {
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
        gameArena.tryEnableTaskIndicators();
        Bukkit.getPluginManager().callEvent(new GameSabotageDeactivateEvent(getArena(), this, false));
        for (Player playing : gameArena.getPlayers()) {
            glowingBox.stopGlowing(playing);
        }
        for (UUID uuid : getOpenGUI()){
            Player player = Bukkit.getPlayer(uuid);
            if (player != null){
                player.closeInventory();
            }
        }
        for (Player player : gameArena.getPlayers()){
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    public GameArena getArena() {
        return gameArena;
    }

    @Override
    public void activate(@Nullable Player player) {
        warning.setBarName(LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, LightsSabotageProvider.NAME_PATH));
        warning.sendBar();
        active = true;
        gameArena.interruptTasks();
        gameArena.disableTaskIndicators();
        Bukkit.getPluginManager().callEvent(new GameSabotageActivateEvent(getArena(), this, player));
        for (Player playing : gameArena.getPlayers()) {
            glowingBox.startGlowing(playing);
        }
        for (Team team : gameArena.getGameTeams()){
            if (!team.getIdentifier().endsWith("-ghost")){
                for (Player member : team.getMembers()){
                    glowingBox.startGlowing(member);
                    if (team.isInnocent()){
                        member.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 5, false), true);
                        member.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 0, false), true);
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
