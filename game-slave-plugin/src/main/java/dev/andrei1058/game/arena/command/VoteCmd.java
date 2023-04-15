package dev.andrei1058.game.arena.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.arena.meeting.MeetingStage;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.arena.meeting.VoteGUIManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class VoteCmd {

    private VoteCmd() {
    }

    public static void register(FastRootCommand root) {
        FastSubCommand vote = new FastSubCommand("vote");
        root.withSubNode(vote
                .withPermAdditions(s -> {
                    if (!(s instanceof Player)) {
                        return false;
                    }
                    Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer((Player) s);
                    if (arena != null) {
                        return arena.isPlayer(((Player) s).getPlayer()) && arena.getMeetingStage() == MeetingStage.VOTING;
                    }
                    return false;
                })
                .withDescription(s -> "")
                .withDisplayHover(s -> "")
                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withExecutor((s, args) -> {
                    Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer((Player) s);
                    assert arena != null;
                    if (args.length == 0) {
                        VoteGUIManager.openToPlayer(((Player) s).getPlayer(), arena);
                    } else if (args[0].equalsIgnoreCase("skip") || args[0].equalsIgnoreCase("null")) {
                        if (arena.getCurrentVoting() != null) {
                            arena.getCurrentVoting().addVote(null, (Player) s, arena);
                        }
                    } else {
                        Player player = Bukkit.getPlayerExact(args[0]);
                        if (player != null) {
                            if (arena.getCurrentVoting() != null) {
                                arena.getCurrentVoting().addVote(player, (Player) s, arena);
                            }
                        }
                    }
                })
        );
    }
}
