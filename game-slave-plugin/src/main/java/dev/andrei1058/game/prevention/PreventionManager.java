package dev.andrei1058.game.prevention;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import dev.andrei1058.game.SteveSus;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.prevention.PreventionHandler;
import dev.andrei1058.game.api.prevention.abandon.AbandonCondition;
import dev.andrei1058.game.api.prevention.abandon.TriggerType;
import dev.andrei1058.game.common.api.arena.GameState;
import dev.andrei1058.game.config.MainConfig;
import dev.andrei1058.game.prevention.abandon.CommandTriggerListener;
import dev.andrei1058.game.prevention.abandon.condition.PlayTimeCondition;
import dev.andrei1058.game.prevention.config.AbusePreventionConfig;
import dev.andrei1058.game.server.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class PreventionManager implements PreventionHandler {

    private static PreventionManager instance;

    // registered condition but not in use
    private final LinkedList<AbandonCondition> registeredAbandonConditions = new LinkedList<>();
    // actual conditions to be checked
    private final LinkedList<AbandonCondition> inUseAbandonConditions = new LinkedList<>();
    // temp cache of abandons if type is COMMAND
    private final LinkedHashMap<UUID, Long> abandoned = new LinkedHashMap<>();
    // cached commands if type is COMMAND
    private final LinkedList<String> commandTriggers = new LinkedList<>();

    private SettingsManager config;
    private TriggerType triggerType;

    private PreventionManager() {
    }

    /**
     * Initialize abandon manager.
     */
    public static void onEnable() {
        if (instance == null) {
            instance = new PreventionManager();

            File targetDir = SteveSus.getInstance().getDataFolder();
            String customPath;
            if (!(customPath = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.ABUSE_PREVENTION_PATH)).isEmpty()) {
                File newPath = new File(customPath);
                if (newPath.isDirectory()) {
                    targetDir = newPath;
                    SteveSus.getInstance().getLogger().info("Set abuse prevention configuration path to: " + targetDir);
                } else {
                    SteveSus.getInstance().getLogger().warning("Tried to set abuse prevention configuration path to: " + targetDir + " but it does not seem like a directory.");
                }
            }
            instance.config = SettingsManagerBuilder.withYamlFile(new File(targetDir, "abuse_prevention.yml")).configurationData(AbusePreventionConfig.class).useDefaultMigrationService().create();

            // enable abuse system if allowed
            if (instance.getConfig().getProperty(AbusePreventionConfig.ABANDON_SYSTEM_ENABLE)) {
                SteveSus.debug("Enabling abandon system..");

                // register default conditions
                instance.registeredAbandonConditions.add(new PlayTimeCondition());
                // parse configured conditions
                instance.loadConditions();

                if ((instance.triggerType = instance.getConfig().getProperty(AbusePreventionConfig.ABANDON_SYSTEM_TRIGGER)) == TriggerType.COMMAND) {
                    // cache commands
                    instance.commandTriggers.addAll(Arrays.asList(instance.getConfig().getProperty(AbusePreventionConfig.ABANDON_COMMANDS).split(",")));
                    // register command listener
                    Bukkit.getPluginManager().registerEvents(new CommandTriggerListener(), SteveSus.getInstance());
                }
                // else checks should be made in hasAbandoned
            }

            // enable anti-farming system if allowed
            if (instance.getConfig().getProperty(AbusePreventionConfig.ANTI_FARMING_ENABLE)) {
                SteveSus.debug("Enabling anti-farming system..");
            }
        }
    }

    @Override
    public boolean isAbandonSystemEnabled() {
        return instance.getConfig().getProperty(AbusePreventionConfig.ABANDON_SYSTEM_ENABLE);
    }

    @Override
    public TriggerType getCurrentAbandonTrigger() {
        return triggerType;
    }

    /**
     * Register an abandon condition.
     * Your condition will only be triggered if there is a
     *
     * @param abandonCondition custom abandon condition.
     * @return false if there is another condition registered with the same identifier or invalid identifier regex.
     */
    public boolean registerAbandonCondition(AbandonCondition abandonCondition) {
        if (!abandonCondition.getIdentifier().matches(AbandonCondition.IDENTIFIER_REGEX)) return false;
        if (registeredAbandonConditions.stream().noneMatch(condition -> condition.getIdentifier().equals(abandonCondition.getIdentifier())) &&
                inUseAbandonConditions.stream().noneMatch(condition -> condition.getIdentifier().equals(abandonCondition.getIdentifier()))) {
            loadConditions();
        }
        return false;
    }

    @Override
    public boolean isAntiFarmingEnabled() {
        return instance.getConfig().getProperty(AbusePreventionConfig.ANTI_FARMING_ENABLE);
    }

    @Override
    public int getMinPlayTime() {
        return instance.getConfig().getProperty(AbusePreventionConfig.ANTI_FARMING_MIN_MATCH_TIME);
    }

    protected void loadConditions() {
        getConfig().reload();
        for (String condition : getConfig().getProperty(AbusePreventionConfig.CONDITIONS).trim().split(",")){
            String[] data = condition.trim().split(":");
            if (data.length != 2 || !data[0].matches(AbandonCondition.IDENTIFIER_REGEX)) {
                SteveSus.getInstance().getLogger().warning("Bad abandon condition: " + condition + ".");
                continue;
            }
            AbandonCondition abandonCondition = registeredAbandonConditions.stream().filter(cond -> cond.getIdentifier().equals(data[0])).findFirst().orElse(null);
            // if there is a registered abandon condition for current string
            if (abandonCondition != null && inUseAbandonConditions.stream().noneMatch(inUse -> inUse.getIdentifier().equals(data[0]))) {
                if (abandonCondition.onLoad(data[1])) {
                    inUseAbandonConditions.add(abandonCondition);
                    registeredAbandonConditions.remove(abandonCondition);
                    SteveSus.debug("Registered abandon condition (now in use): " + data[0]);
                } else {
                    SteveSus.getInstance().getLogger().warning("Could not enable abandon condition: " + data[0]);
                }
            }
        }
    }

    /**
     * Used when type is COMMAND.
     */
    public void setAbandoned(UUID player) {
        abandoned.put(player, System.currentTimeMillis() + 5000);
    }

    /**
     * Check if the given player has abandoned the game.
     */
    public boolean triggerAbandon(Arena arena, Player player) {
        return inUseAbandonConditions.stream().allMatch(condition -> condition.getOutcome(player, arena));
    }

    /**
     * Check if a player abandoned a match.
     * Can be used once because it clears player data after querying.
     */
    public boolean hasAbandoned(@Nullable Arena arena, Player player) {
        if (triggerType == TriggerType.COMMAND) {
            if (abandoned.containsKey(player.getUniqueId())) {
                boolean abandoned = PreventionManager.instance.abandoned.get(player.getUniqueId()) > System.currentTimeMillis();
                PreventionManager.instance.abandoned.remove(player.getUniqueId());
                return abandoned;
            }
        } else if (triggerType == TriggerType.ARENA_LEAVE && arena != null && arena.getGameState() == GameState.IN_GAME) {
            return triggerAbandon(arena, player);
        }
        return false;
    }

    /**
     * To be used when a player is about to get stats.
     * Check if min match time is reached for the given arena.
     */
    public boolean canReceiveStats(Arena arena) {
        if (arena.getGameState() == GameState.WAITING || arena.getGameState() == GameState.STARTING) return false;
        return isAntiFarmingEnabled() && arena.getStartTime() != null && Instant.now().isAfter(arena.getStartTime().plusSeconds(getMinPlayTime()));
    }

    public SettingsManager getConfig() {
        return config;
    }

    public static PreventionManager getInstance() {
        return instance;
    }

    public LinkedList<String> getCommandTriggers() {
        return commandTriggers;
    }
}
