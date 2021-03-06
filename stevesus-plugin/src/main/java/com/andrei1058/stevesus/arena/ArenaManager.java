package com.andrei1058.stevesus.arena;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.SettingsManagerBuilder;
import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.GameEndConditions;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageProvider;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.arena.team.LegacyPlayerColor;
import com.andrei1058.stevesus.api.arena.team.PlayerColorAssigner;
import com.andrei1058.stevesus.api.event.GameInitializedEvent;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.prevention.abandon.AbandonCondition;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.api.world.WorldAdapter;
import com.andrei1058.stevesus.arena.ability.kill.KillListener;
import com.andrei1058.stevesus.arena.command.ForceStartCmd;
import com.andrei1058.stevesus.arena.command.GameCmd;
import com.andrei1058.stevesus.arena.gametask.emptygarbage.EmptyGarbageTaskProvider;
import com.andrei1058.stevesus.arena.gametask.fuelengines.FuelEnginesTaskProvider;
import com.andrei1058.stevesus.arena.gametask.manifolds.UnlockManifoldsProvider;
import com.andrei1058.stevesus.arena.gametask.primeshields.PrimeShieldsTaskProvider;
import com.andrei1058.stevesus.arena.gametask.scan.SubmitScanProvider;
import com.andrei1058.stevesus.arena.gametask.startreactor.StartReactorTaskProvider;
import com.andrei1058.stevesus.arena.gametask.upload.UploadTaskProvider;
import com.andrei1058.stevesus.arena.gametask.wiring.FixWiringProvider;
import com.andrei1058.stevesus.arena.runnable.MapTimeTask;
import com.andrei1058.stevesus.arena.sabotage.fixlights.LightsSabotageProvider;
import com.andrei1058.stevesus.arena.sabotage.oxygen.OxygenSabotageProvider;
import com.andrei1058.stevesus.arena.ability.vent.VentListener;
import com.andrei1058.stevesus.arena.sabotage.reactor.ReactorSabotageProvider;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.common.ServerQuitListener;
import com.andrei1058.stevesus.setup.SetupManager;
import com.andrei1058.stevesus.setup.command.AddCommand;
import com.andrei1058.stevesus.sidebar.GameSidebarManager;
import com.andrei1058.stevesus.worldmanager.WorldManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ArenaManager implements com.andrei1058.stevesus.api.arena.ArenaHandler {

    private static ArenaManager INSTANCE;
    private static final LinkedList<Arena> arenas = new LinkedList<>();
    private static final HashMap<String, Arena> enableQueue = new HashMap<>();
    private static final HashMap<UUID, Arena> arenaByPlayer = new HashMap<>();
    private static final HashMap<String, Arena> arenaByWorldName = new HashMap<>();
    private static final Random randomInstance = new Random();
    private static final LinkedList<TaskProvider> registeredTasks = new LinkedList<>();
    private static final LinkedList<SabotageProvider> registeredSabotages = new LinkedList<>();

    private static long lastPlayerCountRequest = 0L;
    private static int lastPlayerCount = 0;
    private static long lastSpectatorCountRequest = 0L;
    private static int lastSpectatorCount = 0;

    private static File arenaDirectory = new File(SteveSus.getInstance().getDataFolder(), "Templates");

    // this is used to identify game maps in bukkit world's container. should never be empty.
    public static final String WORLD_NAME_SEPARATOR = "_game_";

    private static int gameId = 0;

    private final GameEndConditions gameEndConditions = new GameEndConditions();
    private PlayerColorAssigner<PlayerColorAssigner.PlayerColor> defaultColorAssigner;

    private ArenaManager() {

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
        defaultColorAssigner = new PlayerColorAssigner<>();
        for (LegacyPlayerColor color : LegacyPlayerColor.values()) {
            defaultColorAssigner.addColorOption(color);
        }
    }

    public static void onEnable() {
        if (INSTANCE != null) return;
        INSTANCE = new ArenaManager();

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
            GameSidebarManager.getInstance().removeSidebar(p);
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
        getINSTANCE().registerGameTask(FixWiringProvider.getInstance());
        getINSTANCE().registerGameTask(SubmitScanProvider.getInstance());
        getINSTANCE().registerGameTask(UploadTaskProvider.getInstance());
        getINSTANCE().registerGameTask(UnlockManifoldsProvider.getInstance());
        getINSTANCE().registerGameTask(EmptyGarbageTaskProvider.getInstance());
        getINSTANCE().registerGameTask(FuelEnginesTaskProvider.getInstance());
        getINSTANCE().registerGameTask(StartReactorTaskProvider.getInstance());
        getINSTANCE().registerGameTask(PrimeShieldsTaskProvider.getInstance());

        // register default sabotages
        getINSTANCE().registerSabotage(OxygenSabotageProvider.getInstance());
        getINSTANCE().registerSabotage(LightsSabotageProvider.getInstance());
        getINSTANCE().registerSabotage(ReactorSabotageProvider.getInstance());
    }

    public static void onDisable() {
        if (INSTANCE == null) return;
        long startTime = System.currentTimeMillis();
        SteveSus.getInstance().getLogger().info("Disabling arenas..");
        new ArrayList<>(INSTANCE.getEnableQueue()).forEach(arena -> INSTANCE.disableArena(arena));
        new ArrayList<>(INSTANCE.getArenas()).forEach(arena -> INSTANCE.disableArena(arena));
        SteveSus.debug("Took " + (System.currentTimeMillis() - startTime) + "ms to disable " + ArenaManager.class.getSimpleName() + ".");
    }

    public static ArenaManager getINSTANCE() {
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
        arenaByWorldName.put(arena.getWorld().getName(), arena);
        // register listeners
        arena.registerGameListener(VentListener.getInstance());
        arena.registerGameListener(new KillListener());
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
        //if (config.getProperty(ArenaConfig.MEETING_LOCATIONS).isEmpty()) return false;
        //if (!config.getProperty(ArenaConfig.MEETING_BUTTON_LOC).isPresent()) return false;
        //if (config.getProperty(ArenaConfig.VENTS).isEmpty()) return false;
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean registerGameTask(TaskProvider taskProvider) {
        if (registeredTasks.contains(taskProvider)) return false;
        LanguageManager.getINSTANCE().getDefaultLocale().setMsg(Message.GAME_TASK_NAME_PATH_.toString() + taskProvider.getIdentifier(), taskProvider.getDefaultDisplayName());
        LanguageManager.getINSTANCE().getDefaultLocale().setMsg(Message.GAME_TASK_DESCRIPTION_PATH_.toString() + taskProvider.getIdentifier(), taskProvider.getDefaultDescription());

        if (SteveSus.getInstance().getMainCommand() != null) {
            FastSubRootCommand add = (FastSubRootCommand) SteveSus.getInstance().getMainCommand().getSubCommand("add");
            if (add != null) {
                FastSubRootCommand task = (FastSubRootCommand) add.getSubCommand("task");
                if (task != null) {
                    FastSubCommand providerCommand = (FastSubCommand) task.getSubCommand(taskProvider.getProvider().getName());
                    if (providerCommand == null) {
                        providerCommand = new FastSubCommand(taskProvider.getProvider().getName());
                        task.withSubNode(providerCommand);

                        FastSubCommand finalProviderCommand = providerCommand;
                        providerCommand.withTabSuggestions(s -> {
                            List<String> list = new ArrayList<>();
                            for (TaskProvider taskProvider1 : ArenaManager.getINSTANCE().getRegisteredTasks()) {
                                if (taskProvider1.getProvider().getName().equals(taskProvider.getProvider().getName())) {
                                    list.add(taskProvider1.getIdentifier());
                                }
                            }
                            return list;
                        });
                        providerCommand.withExecutor((sender, args) -> {
                            if (args.length != 2) {
                                sender.sendMessage(" ");
                                String command = ICommandNode.getClickCommand(finalProviderCommand);
                                TextComponent usage = new TextComponent(ChatColor.RED + "Usage: " + ChatColor.GRAY + command);
                                usage.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
                                TextComponent taskHolder = new TextComponent(" [task]");
                                taskHolder.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(ChatColor.YELLOW + "[task] " + ChatColor.GRAY + "is the task name from provider.")}));
                                usage.addExtra(taskHolder);
                                TextComponent localIdentifier = new TextComponent(" [localIdentifier]");
                                localIdentifier.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(ChatColor.YELLOW + "[localIdentifier] " + ChatColor.GRAY + "is some name that helps you remember this configuration, because you can set a task multiple times and this name is used eventually later if you want to remove this configuration.")}));
                                usage.addExtra(localIdentifier);
                                sender.spigot().sendMessage(usage);
                                sender.sendMessage(" ");
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&1|| &3Tasks provided by &e" + taskProvider.getProvider().getName()));

                                for (TaskProvider taskHandler : ArenaManager.getINSTANCE().getRegisteredTasks()) {
                                    if (taskHandler.getProvider().getName().equals(taskProvider.getProvider().getName())) {
                                        TextComponent textComponent = new TextComponent(ChatColor.GOLD + "- " + ChatColor.GRAY + taskHandler.getProvider().getName() + " " + ChatColor.YELLOW + taskHandler.getIdentifier());
                                        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{new TextComponent(ChatColor.WHITE + "Click to use " + ChatColor.translateAlternateColorCodes('&', taskHandler.getDefaultDisplayName()))}));
                                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command + taskHandler.getIdentifier() + " "));
                                        sender.spigot().sendMessage(textComponent);
                                    }
                                }
                                return;
                            }

                            TaskProvider taskRegisteredProvider = ArenaManager.getINSTANCE().getTask(taskProvider.getProvider().getName(), args[0]);
                            if (taskRegisteredProvider == null) {
                                sender.sendMessage(ChatColor.RED + "Invalid provider or task identifier.");
                                return;
                            }
                            Player player = (Player) sender;
                            SetupSession setupSession = SetupManager.getINSTANCE().getSession(player);
                            if (!taskRegisteredProvider.canSetup(player, setupSession)) {
                                player.sendMessage(ChatColor.RED + "You're not allowed to set this task on this map. (This task might allow to be set a single time per template).");
                                return;
                            }
                            if (!args[1].matches(AbandonCondition.IDENTIFIER_REGEX)) {
                                player.sendMessage(ChatColor.RED + args[1] + ChatColor.GRAY + " cannot be used. Try removing special characters.");
                                return;
                            }

                            if (AddCommand.hasTaskWithRememberName(player, args[1])) {
                                player.sendMessage(ChatColor.RED + args[1] + ChatColor.GRAY + " already exists. Please give it another name (it's used to recognize it if you want to remove it eventually).");
                                return;
                            }
                            player.sendMessage(ChatColor.GRAY + "Disabling commands usage...");
                            assert setupSession != null;
                            setupSession.setAllowCommands(false);
                            taskRegisteredProvider.onSetupRequest(player, setupSession, args[1]);
                        });
                    }
                }
            }
        }
        return registeredTasks.add(taskProvider);
    }

    @Override
    public List<TaskProvider> getRegisteredTasks() {
        return Collections.unmodifiableList(registeredTasks);
    }

    @Override
    @Nullable
    public TaskProvider getTask(String provider, String task2) {
        return registeredTasks.stream().filter(task -> task.getProvider().getName().equals(provider) && task.getIdentifier().equals(task2)).findFirst().orElse(null);
    }

    @Override
    public void saveTaskData(TaskProvider task, SetupSession setupSession, String givenName, JsonObject taskConfiguration) {
        SettingsManager config = getTemplate(setupSession.getWorldName(), true);
        List<String> tasks = new ArrayList<>(config.getProperty(ArenaConfig.TASKS));
        SteveSus.debug("Saving " + task.getIdentifier() + "(" + givenName + ") task data on " + setupSession.getWorldName() + ".");
        tasks.add(givenName + ";" + task.getProvider().getName() + ";" + task.getIdentifier() + ";" + taskConfiguration.toString());
        config.setProperty(ArenaConfig.TASKS, tasks);
        config.save();
    }

    @Override
    public void deleteTaskData(SetupSession setupSession, String givenName) {
        SettingsManager config = getTemplate(setupSession.getWorldName(), true);
        List<String> tasks = new ArrayList<>(config.getProperty(ArenaConfig.TASKS));
        SteveSus.debug("Removing " + givenName + " task data from " + setupSession.getWorldName() + ".");
        tasks.removeIf(taskString -> {
            if (taskString.startsWith(givenName + ";")) {
                String[] taskData = taskString.split(";");
                if (taskData.length == 4) {
                    TaskProvider taskProvider = ArenaManager.getINSTANCE().getTask(taskData[1], taskData[2]);
                    if (taskProvider != null) {
                        taskProvider.onRemove(setupSession, givenName, new JsonParser().parse(taskData[3]).getAsJsonObject());
                    }
                }
                return true;
            }
            return false;
        });
        config.setProperty(ArenaConfig.TASKS, tasks);
        config.save();
    }

    @Override
    public @Nullable PlayerColorAssigner<PlayerColorAssigner.PlayerColor> getDefaultPlayerColorAssigner() {
        return defaultColorAssigner;
    }

    @Override
    public void setDefaultPlayerColorAssigner(@Nullable PlayerColorAssigner<PlayerColorAssigner.PlayerColor> defaultPlayerColorAssigner) {
        if (this.defaultColorAssigner != null) {
            getArenas().stream().filter(arena -> arena.getPlayerColorAssigner() != null & arena.getPlayerColorAssigner().equals(this.defaultColorAssigner))
                    .forEach(arena -> arena.getPlayers().forEach(player -> this.defaultColorAssigner.restorePlayer(player)));
        }
        this.defaultColorAssigner = defaultPlayerColorAssigner;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public boolean registerSabotage(SabotageProvider sabotageProvider) {
        if (registeredSabotages.contains(sabotageProvider)) {
            return false;
        }
        if (SetupManager.getINSTANCE().getSetSabotageCommand().getSubCommand(sabotageProvider.getOwner().getName()) == null) {
            FastSubRootCommand cmd = new FastSubRootCommand(sabotageProvider.getOwner().getName()).withDisplayHover(s -> "&1|| &3Sabotages provided by " + sabotageProvider.getOwner().getName());
            SetupManager.getINSTANCE().getSetSabotageCommand().withSubNode(cmd);
        }
        sabotageProvider.onRegister();
        return registeredSabotages.add(sabotageProvider);
    }

    @Override
    public @Nullable SabotageProvider getSabotageProviderByName(String pluginOwner, String sabotageIdentifier) {
        return registeredSabotages.stream().filter(sabotage -> sabotage.getOwner().getName().equals(pluginOwner) && sabotage.getUniqueIdentifier().equals(sabotageIdentifier)).findFirst().orElse(null);
    }

    @Override
    public boolean saveSabotageConfiguration(String template, SabotageProvider sabotageProvider, JsonObject configuration, boolean replaceExisting) {
        SettingsManager config = getTemplate(template, true);
        List<String> sabotages = new ArrayList<>(config.getProperty(ArenaConfig.SABOTAGES));
        String sabotageString = sabotages.stream().filter(string -> string.startsWith(sabotageProvider.getOwner().getName() + ";" + sabotageProvider.getUniqueIdentifier())).findFirst().orElse(null);
        if (sabotageString != null) {
            if (replaceExisting) {
                sabotages.remove(sabotageString);
            } else {
                // do not replace existing data
                return false;
            }
        }
        sabotages.add(sabotageProvider.getOwner().getName() + ";" + sabotageProvider.getUniqueIdentifier() + ";" + configuration.toString());
        config.setProperty(ArenaConfig.SABOTAGES, sabotages);
        config.save();
        return true;
    }

    @Override
    public @Nullable JsonObject getSabotageConfiguration(String template, SabotageProvider provider) {
        SettingsManager config = getTemplate(template, true);
        List<String> sabotages = config.getProperty(ArenaConfig.SABOTAGES);
        String sabotageString = sabotages.stream().filter(string -> string.startsWith(provider.getOwner().getName() + ";" + provider.getUniqueIdentifier())).findFirst().orElse(null);
        if (sabotageString == null) {
            return null;
        }
        String[] data = sabotageString.split(";");
        if (data.length < 3) return null;
        return new JsonParser().parse(data[2]).getAsJsonObject();
    }

    @Override
    public List<SabotageProvider> getRegisteredSabotages() {
        return registeredSabotages;
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

    public GameEndConditions getGameEndConditions() {
        return gameEndConditions;
    }
}
