package com.andrei1058.amongusmc.teleporter;

import com.andrei1058.amongusmc.api.arena.Arena;
import com.andrei1058.amongusmc.arena.ArenaManager;
import com.andrei1058.amongusmc.arena.command.TeleporterCmd;
import com.andrei1058.amongusmc.common.CommonManager;
import com.andrei1058.amongusmc.common.stats.config.StatsConfig;
import com.andrei1058.amongusmc.config.ArenaConfig;
import com.andrei1058.amongusmc.language.LanguageManager;
import com.andrei1058.amongusmc.teleporter.config.TeleporterConfig;
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
     * @param arena  player's arena.
     */
    public static void openToPlayer(@NotNull Player player, @NotNull Arena arena) {
        String guiName = ArenaManager.getINSTANCE().getTemplate(arena.getTemplateWorld(), true).getProperty(ArenaConfig.TELEPORTER_LAYOUT);
        if (!getInstance().hasTeleporter(guiName)){
            guiName = "main";
        }
        // load async
        String finalGuiName = guiName;
        Bukkit.getScheduler().runTaskAsynchronously(CommonManager.getINSTANCE().getPlugin(), () -> {
            List<String> pattern = getInstance().getTeleporterConfig().getList(finalGuiName + "." + StatsConfig.STATS_GENERIC_PATTERN_PATH);
            TeleporterGUI gui = new TeleporterGUI(finalGuiName, pattern, player, LanguageManager.getINSTANCE().getLocale(player), arena);
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
