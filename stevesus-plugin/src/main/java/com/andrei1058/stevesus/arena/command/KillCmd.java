package com.andrei1058.stevesus.arena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class KillCmd {

    private KillCmd() {
    }

    public static void register(FastRootCommand root) {
        FastSubCommand vote = new FastSubCommand("kill");
        root.withSubNode(vote
                .withPermAdditions(s -> {
                    if (!(s instanceof Player)) {
                        return false;
                    }
                    Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer((Player) s);
                    if (arena != null) {
                        Team playerTeam = arena.getPlayerTeam((Player) s);
                        return arena.isPlayer(((Player) s).getPlayer()) && arena.getGameState() == GameState.IN_GAME && playerTeam != null && !playerTeam.isInnocent() && !playerTeam.getIdentifier().contains("-ghost");
                    }
                    return false;
                })
                .withDescription(s -> "")
                .withDisplayHover(s -> "")
                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withExecutor((s, args) -> {
                    Player player = (Player) s;
                    Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(player);
                    assert arena != null;
                    Player nearest = null;
                    double distance = arena.getKillDistance();
                    Team playerTeam = arena.getPlayerTeam(player);
                    for (Player inGame : arena.getPlayers()) {
                        if (player.equals(inGame)) continue;
                        double currentDistance;
                        if ((currentDistance = player.getLocation().distance(inGame.getLocation())) < distance && playerTeam.canKill(inGame)) {
                            nearest = inGame;
                            distance = currentDistance;
                        }
                    }
                    if (nearest != null){
                        arena.killPlayer(player, nearest);
                    }
                })
        );
    }
}
