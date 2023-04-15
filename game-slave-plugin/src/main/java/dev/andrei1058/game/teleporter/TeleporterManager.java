package dev.andrei1058.game.teleporter;

import dev.andrei1058.game.api.arena.GameArena;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.arena.command.TeleporterCmd;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.stats.config.StatsConfig;
import dev.andrei1058.game.config.ArenaConfig;
import dev.andrei1058.game.language.LanguageManager;
import dev.andrei1058.game.teleporter.config.TeleporterConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class TeleporterManager {

    private static TeleporterManager instance;

    private TeleporterConfig teleporterConfig;

    public static TeleporterManager getInstance() {
        return instance;
    }

    private TeleporterManager() {
    }

    /**
     * Initialize stats manager.
     *
     * @param teleporterFileDirectory directory where to get .yml from.
     */
    public static void init(File teleporterFileDirectory) {
        if (instance == null) {
            instance = new TeleporterManager();
            instance.teleporterConfig = new TeleporterConfig(teleporterFileDirectory, "layout_teleporter");
            TeleporterCmd.register(CommonManager.getINSTANCE().getCommonProvider().getMainCommand());
        }
    }

    /**
     * Open a teleporter GUI to a player.
     *
     * @param player player receiver.
     * @param gameArena  player's arena.
     */
    public static void openToPlayer(@NotNull Player player, @NotNull GameArena gameArena) {
        String guiName = ArenaManager.getINSTANCE().getTemplate(gameArena.getTemplateWorld(), true).getProperty(ArenaConfig.TELEPORTER_LAYOUT);
        if (!getInstance().hasTeleporter(guiName)){
            guiName = "main";
        }
        // load async
        String finalGuiName = guiName;
        Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> {
            List<String> pattern = getInstance().getTeleporterConfig().getList(finalGuiName + "." + StatsConfig.STATS_GENERIC_PATTERN_PATH);
            TeleporterGUI gui = new TeleporterGUI(finalGuiName, pattern, player, LanguageManager.getINSTANCE().getLocale(player), gameArena);
            // open sync
            Bukkit.getScheduler().runTask(CommonManager.getINSTANCE().getPlugin(), () -> gui.open(player));
        });
    }

    /**
     * Check if the given teleporter exists.
     */
    private boolean hasTeleporter(String name) {
        return !name.isEmpty() && getTeleporterConfig().getYml().get(name) != null;
    }


    public TeleporterConfig getTeleporterConfig() {
        return teleporterConfig;
    }
}
