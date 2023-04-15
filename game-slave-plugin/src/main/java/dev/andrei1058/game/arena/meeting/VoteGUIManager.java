package dev.andrei1058.game.arena.meeting;

import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.arena.command.VoteCmd;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.config.ArenaConfig;
import dev.andrei1058.game.language.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class VoteGUIManager {

    private static VoteGUIManager instance;

    private VoteLayoutConfig voteLayoutConfig;

    public static VoteGUIManager getInstance() {
        return instance;
    }

    private VoteGUIManager() {
    }

    /**
     * Initialize stats manager.
     *
     * @param directory directory where to get .yml from.
     */
    public static void init(File directory) {
        if (instance == null) {
            instance = new VoteGUIManager();
            instance.voteLayoutConfig = new VoteLayoutConfig(directory, "layout_exclusion");
            VoteCmd.register(CommonManager.getINSTANCE().getCommonProvider().getMainCommand());
        }
    }

    /**
     * Open a GUI to a player.
     *
     * @param player player receiver.
     * @param arena  player's arena.
     */
    public static void openToPlayer(@NotNull Player player, @NotNull Arena arena) {
        String guiName = ArenaManager.getINSTANCE().getTemplate(arena.getTemplateWorld(), true).getProperty(ArenaConfig.EXCLUSION_LAYOUT);
        if (!getInstance().hasExclusionGUI(guiName)){
            guiName = "main";
        }
        // load async
        String finalGuiName = guiName;
        Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> {
            List<String> pattern = getInstance().getConfig().getList(finalGuiName + "." + VoteLayoutConfig.VOTE_GENERIC_PATTERN_PATH);
            ExclusionGUI gui = new ExclusionGUI(finalGuiName, pattern, player, LanguageManager.getINSTANCE().getLocale(player), arena);
            // open sync
            Bukkit.getScheduler().runTask(CommonManager.getINSTANCE().getPlugin(), () -> gui.open(player));
        });
    }

    /**
     * Check if the given exclusion exists.
     */
    private boolean hasExclusionGUI(String name) {
        return !name.isEmpty() && getConfig().getYml().get(name) != null;
    }


    public VoteLayoutConfig getConfig() {
        return voteLayoutConfig;
    }
}
