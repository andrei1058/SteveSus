package com.andrei1058.stevesus.arena;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.task.TaskHandler;
import com.andrei1058.stevesus.api.event.GameInitializedEvent;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.api.world.WorldAdapter;
import com.andrei1058.stevesus.arena.command.ForceStartCmd;
import com.andrei1058.stevesus.arena.command.GameCmd;
import com.andrei1058.stevesus.arena.gametask.wiring.FixWiringHandler;
import com.andrei1058.stevesus.arena.runnable.MapTimeTask;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.common.ServerQuitListener;
import com.andrei1058.stevesus.worldmanager.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaHandler implements com.andrei1058.stevesus.api.arena.ArenaHandler {

    private static ArenaHandler INSTANCE;
    private static final LinkedList<Arena> arenas = new LinkedList<>();
    private static final HashMap<String, Arena> enableQueue = new HashMap<>();
    private static final HashMap<UUID, Arena> arenaByPlayer = new HashMap<>();
    private static final HashMap<String, Arena> arenaByWorldName = new HashMap<>();
    private static final Random randomInstance = new Random();
    private static final LinkedList<TaskHandler> registeredTasks = new LinkedList<>();

    private static long lastPlayerCountRequest = 0L;
    private static int lastPlayerCount = 0;
    private static long lastSpectatorCountRequest = 0L;
    private static int lastSpectatorCount = 0;

    private static File arenaDirectory = new File(SteveSus.getInstance().getDataFolder(), "Templates");

    // this is used to identify game maps in bukkit world's container. should never be empty.
    public static final String WORLD_NAME_SEPARATOR = "_game_";

    private static int gameId = 0;

    private ArenaHandler() {

        // change templates path eventually
        String newPath = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.TEMPLATES_PATH);
        if (!newPath.isEmpty()) {
            File newDirectory = new File(newPath);
            if (newDirectory.isDirectory()) {
                arenaDirectory = newDirectory;
                SteveSus.getInstance().getLogger().info("Set templates path to: " + newPath);
            } else {
                SteveSus.getInstance().getLogger().warning("Tried to set templates path to: " + newPath + " but it does not seem like a directory.");
            }
        }

        if (!arenaDirectory.exists()) {
            //noinspection ResultOfMethodCallIgnored
            arenaDirectory.mkdir();
        }
    }

    public static void onEnable() {
        if (INSTANCE != null) return;
        INSTANCE = new ArenaHandler();

        // register internal quit listener
        ServerQuitListener.registerInternalQuit((p) -> {
            // Remove from arena
            Arena a = getINSTANCE().getArenaByPlayer(p);
            if (a != null) {
                if (a.isPlayer(p)) {
                    a.removePlayer(p, true);
                } else if (a.isSpectator(p)) {
                    a.removeSpectator(p, true);
                }
            }
        });

        // create arenas from templates
        if (arenaDirectory.exists()) {
            for (File file : Objects.requireNonNull(arenaDirectory.listFiles())) {
                if (file != null && file.isFile()) {
                    if (file.getName().endsWith(".yml")) {
                        String templateName = file.getName().replace(".yml", "");
                        if (INSTANCE.validateTemplate(templateName)) {
                            SettingsManager template = INSTANCE.getTemplate(templateName, false);
                            int availableAtOnce = template.getProperty(ArenaConfig.CLONES_AVAILABLE_AT_ONCE);
                            if (template.getProperty(ArenaConfig.LOAD_AT_START_UP)) {
                                SteveSusArena arena = new SteveSusArena(templateName, INSTANCE.getNextGameId());
                                INSTANCE.addToEnableQueue(arena);
                                if (availableAtOnce > 1) {
                                    for (int i = 1; i < availableAtOnce; i++) {
                                        SteveSusArena clonedArena = new SteveSusArena(templateName, INSTANCE.getNextGameId());
                                        INSTANCE.addToEnableQueue(clonedArena);
                                    }
                                }
                            }
                        } else {
                            SteveSus.getInstance().getLogger().warning("Cannot create arena from template: " + templateName);
                        }
                    }
                }
            }
        }

        // register arena related commands
        GameCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
        ForceStartCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());

        // register map time checker
        Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), new MapTimeTask(), 20L, 20L);

        // register default tasks
        getINSTANCE().registerGameTask(FixWiringHandler.getInstance());
    }

    public static void onDisable() {
        if (INSTANCE == null) return;
        long startTime = System.currentTimeMillis();
        SteveSus.getInstance().getLogger().info("Disabling arenas..");
        new ArrayList<>(INSTANCE.getEnableQueue()).forEach(arena -> INSTANCE.disableArena(arena));
        new ArrayList<>(INSTANCE.getArenas()).forEach(arena -> INSTANCE.disableArena(arena));
        SteveSus.debug("Took " + (System.currentTimeMillis() - startTime) + "ms to disable " + ArenaHandler.class.getSimpleName() + ".");
    }

    public static ArenaHandler getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public int getNextGameId() {
        return gameId++;
    }

    @Override
    public @Nullable Arena getArenaById(int id) {
        return arenas.stream().filter(arena -> arena.getGameId() == id).findFirst().orElse(null);
    }

    @Override
    public void startArenaFromTemplate(String worldName) {
        SteveSus.debug("Starting arena from template: " + worldName);

        // check configuration
        File template = getTemplateFile(worldName);
        if (!template.exists()) {
            SteveSus.getInstance().getLogger().warning("Could not load arena from template: " + worldName + ". File does not exist: " + template.getPath());
            return;
        }

        // check if world exists
        WorldAdapter worldAdapter = WorldManager.getINSTANCE().getWorldAdapter();
        if (!worldAdapter.hasWorld(worldName)) {
            SteveSus.getInstance().getLogger().warning("Could not load arena from template: " + worldName + ". World does not exists in " + worldAdapter.getAdapterName() + "'s container.");
            return;
        }

        // add to enable queue
        Arena arena = new SteveSusArena(worldName, getNextGameId());
        addToEnableQueue(arena);
    }

    @Override
    public boolean addArena(Arena arena) {
        if (getArenaById(arena.getGameId()) != null) return false;
        if (arenas.contains(arena)) return false;
        SteveSus.debug("Adding arena with id " + arena.getGameId() + " from template: " + arena.getTemplateWorld() + " to the arenas list.");
        Bukkit.getPluginManager().callEvent(new GameInitializedEvent(arena, arena.getTemplateWorld(), arena.getWorld().getName()));
        arenaByWorldName.put(arena.getTemplateWorld(), arena);
        return arenas.add(arena);
    }

    @Override
    public boolean addToEnableQueue(Arena arena) {
        String gameWorld = arena.getTemplateWorld() + WORLD_NAME_SEPARATOR + arena.getGameId();
        if (enableQueue.containsKey(gameWorld)) return false;
        SteveSus.debug("Adding to enable queue arena with id " + arena.getGameId() + " from template: " + arena.getTemplateWorld());
        enableQueue.put(gameWorld, arena);
        // a bit of delay to prevent issues when instantiating multiple arenas of the same type
        SteveSus.newChain().delay(5 + randomInstance.nextInt(5)).sync(() -> WorldManager.getINSTANCE().getWorldAdapter().onArenaEnableQueue(arena.getTemplateWorld(), arena)).execute();
        return true;
    }

    @Override
    public void removeFromEnableQueue(String gameWorld) {
        enableQueue.remove(gameWorld);
        SteveSus.debug("Removing from enable queue game world: " + gameWorld);
    }

    @Override
    public Arena getFromEnableQueue(String gameWorld) {
        return enableQueue.get(gameWorld);
    }

    @Override
    public void removeArena(Arena arena) {
        if (arena.getWorld() != null) {
            arenaByWorldName.remove(arena.getWorld().getName());
        }
        if (arenas.remove(arena)) {
            SteveSus.debug("Removed arena with id " + arena.getGameId() + " from template: " + arena.getTemplateWorld() + " to the arenas list.");
        }
    }

    @Override
    public File getTemplatesDirectory() {
        return arenaDirectory;
    }

    @Override
    public File getTemplateFile(String worldName) {
        return new File(getTemplatesDirectory(), worldName + ".yml");
    }

    @Override
    public List<String> getTemplates() {
        List<String> list = new ArrayList<>();
        if (getTemplatesDirectory().exists()) {
            for (File file : Objects.requireNonNull(getTemplatesDirectory().listFiles())) {
                if (file != null && file.isFile()) {
                    if (file.getName().endsWith(".yml")) {
                        list.add(file.getName().replace(".yml", ""));
                    }
                }
            }
        }
        return list;
    }

    @Override
    public boolean hasVipJoin(Player player) {
        return CommonManager.getINSTANCE().hasVipJoin(player);
    }

    @Override
    public boolean isInArena(Player player) {
        return arenaByPlayer.containsKey(player.getUniqueId());
    }

    @Override
    public boolean isSpectating(Player player) {
        Arena arena = getArenaByPlayer(player);
        return arena != null && arena.isSpectator(player);
    }

    @Override
    public List<Arena> getArenas() {
        return Collections.unmodifiableList(arenas);
    }

    @Override
    public List<Arena> getEnableQueue() {
        return new ArrayList<>(enableQueue.values());
    }

    @Override
    public @Nullable Arena getArenaByPlayer(Player player) {
        return arenaByPlayer.get(player.getUniqueId());
    }

    @Override
    public @Nullable Arena getArenaByWorld(@NotNull String worldName) {
        return arenaByWorldName.get(worldName);
    }

    @Override
    public void setArenaByPlayer(Player player, @Nullable Arena arena) {
        if (arena == null) {
            arenaByPlayer.remove(player.getUniqueId());
            return;
        }
        if (arenaByPlayer.containsKey(player.getUniqueId())) {
            arenaByPlayer.replace(player.getUniqueId(), arena);
        } else {
            arenaByPlayer.put(player.getUniqueId(), arena);
        }
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean validateTemplate(String templateName) {
        SettingsManager config = getTemplate(templateName, false);
        if (config == null) {
            return false;
        }
        // todo add validation checks
        if (config.getProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS).isEmpty()) return false;
        if (config.getProperty(ArenaConfig.SPECTATE_LOCATIONS).isEmpty()) return false;
        if (config.getProperty(ArenaConfig.MEETING_LOCATIONS).isEmpty()) return false;
        if (!config.getProperty(ArenaConfig.MEETING_BUTTON_LOC).isPresent()) return false;
        if (config.getProperty(ArenaConfig.VENTS).isEmpty()) return false;
        return true;
    }

    @Override
    public void disableArena(Arena arena) {
        INSTANCE.removeArena(arena);
        arena.disable();
        WorldManager.getINSTANCE().getWorldAdapter().onArenaDisable(arena);
        arenas.remove(arena);
        if (arena.getWorld() != null) {
            arenaByWorldName.remove(arena.getWorld().getName());
        }
    }

    @Override
    public int getOnlineCount() {
        return arenaByPlayer.size();
    }

    @Override
    public int getPlayerCount() {
        if (System.currentTimeMillis() < lastPlayerCountRequest) {
            return lastPlayerCount;
        }
        lastPlayerCount = 0;
        arenas.forEach(arena -> lastPlayerCount += arena.getPlayers().size());
        // 50 should be a server tick
        lastPlayerCountRequest = System.currentTimeMillis() + 50;
        return lastPlayerCount;
    }

    @Override
    public int getSpectatorCount() {
        if (System.currentTimeMillis() < lastSpectatorCountRequest) {
            return lastSpectatorCount;
        }
        lastSpectatorCount = 0;
        arenas.forEach(arena -> lastSpectatorCount += arena.getPlayers().size());
        // 50 should be a server tick
        lastSpectatorCountRequest = System.currentTimeMillis() + 50;
        return lastSpectatorCount;
    }

    @Override
    public boolean registerGameTask(TaskHandler taskHandler) {
        if (registeredTasks.contains(taskHandler)) return false;
        return registeredTasks.add(taskHandler);
    }

    @Override
    public List<TaskHandler> getRegisteredTasks() {
        return Collections.unmodifiableList(registeredTasks);
    }

    @Override
    @Nullable
    public TaskHandler getTask(String provider, String task2) {
        return registeredTasks.stream().filter(task -> task.getProvider().getName().equals(provider) && task.getIdentifier().equals(task2)).findFirst().orElse(null);
    }

    @Override
    public void saveTaskData(TaskHandler task, SetupSession setupSession, String givenName) {
        SettingsManager config = getTemplate(setupSession.getWorldName(), true);
        List<String> tasks = new ArrayList<>(config.getProperty(ArenaConfig.TASKS));
        SteveSus.debug("Saving " + task.getIdentifier() + "(" + givenName + ") task data on " + setupSession.getWorldName() + ".");
        tasks.add(givenName + ";" + task.getProvider().getName() + ";" + task.getIdentifier() + ";" + task.exportAndSave(setupSession).toJSONString());
        config.setProperty(ArenaConfig.TASKS, tasks);
        config.save();
    }

    public SettingsManager getTemplate(String worldName, boolean create) {
        File templateFile = getTemplateFile(worldName);
        if (!templateFile.exists() && create) {
            try {
                //noinspection ResultOfMethodCallIgnored
                templateFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return SettingsManagerBuilder.withYamlFile(templateFile).configurationData(ArenaConfig.class).useDefaultMigrationService().create();
    }
}
