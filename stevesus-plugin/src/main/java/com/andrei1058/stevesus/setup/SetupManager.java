package com.andrei1058.stevesus.setup;

import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.task.TaskProvider;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.api.setup.SetupHandler;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaManager;
import com.andrei1058.stevesus.command.SlaveCommandManager;
import com.andrei1058.stevesus.common.command.CommonCmdManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.common.ServerQuitListener;
import com.andrei1058.stevesus.server.multiarena.command.BuildCmd;
import com.andrei1058.stevesus.server.multiarena.command.SetLobbyCmd;
import com.andrei1058.stevesus.setup.command.ArenaCommands;
import com.andrei1058.stevesus.setup.command.SetupCommand;
import com.andrei1058.stevesus.setup.listeners.CreatureSpawnListener;
import com.andrei1058.stevesus.setup.listeners.SetupSessionListener;
import com.andrei1058.stevesus.setup.listeners.WorldLoadListener;
import com.andrei1058.stevesus.setup.listeners.WorldProtectListener;
import com.andrei1058.stevesus.worldmanager.WorldManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class SetupManager implements SetupHandler {

    private static SetupManager INSTANCE;

    private final LinkedList<SetupSession> setupSessions = new LinkedList<>();
    //private final Logger logger = LoggerFactory.getLogger(LanguageManager.class);

    // setup listener used by setup sessions
    private SetupSessionListener setupSessionListener;

    private SetupManager() {
        INSTANCE = this;
        //logger.debug("SetupManager initialized!");

        // register setup related listeners
        for (Listener listener : new Listener[]{new WorldLoadListener(), new CreatureSpawnListener(), new WorldProtectListener()}) {
            Bukkit.getPluginManager().registerEvents(listener, SteveSus.getInstance());
        }

        // register internal quit listener
        ServerQuitListener.registerInternalQuit((p) -> {
            // remove existing setup session
            SetupSession setupSession = SetupManager.getINSTANCE().getSession(p);
            if (setupSession == null) return;
            SetupManager.getINSTANCE().removeSession(setupSession);
        });
    }

    public static void init() {
        if (INSTANCE != null) return;
        new SetupManager();

        // add setup command
        CommonCmdManager.getINSTANCE().getMainCmd().withSubNode(new SetupCommand("setup").withPriority(-0.1));

        // add arena setup commands to the main command
        ArenaCommands.register(CommonCmdManager.getINSTANCE().getMainCmd());

        // register setLobby command for multi-arena
        if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
            SetLobbyCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
            BuildCmd.register(CommonCmdManager.getINSTANCE().getMainCmd());
        }
    }

    public static SetupManager getINSTANCE() {
        return INSTANCE;
    }

    public List<SetupSession> getSetupSessions() {
        return Collections.unmodifiableList(setupSessions);
    }

    @Override
    public void addSession(final SetupSession setupSession) {
        if (setupSessions.stream().noneMatch(ss -> ss.equals(setupSession))) {
            setupSessions.add(setupSession);
            WorldManager.getINSTANCE().getWorldAdapter().onSetupSessionStart(setupSession.getWorldName(), setupSession);
            if (setupSessionListener == null) {
                setupSessionListener = new SetupSessionListener();
                Bukkit.getPluginManager().registerEvents(setupSessionListener, SteveSus.getInstance());
            }
            //logger.debug("SetupSession added: " + setupSession.toString());
        }
    }

    @Override
    public void removeSession(SetupSession setupSession) {
        if (setupSessions.remove(setupSession)) {
            SetupManager.getINSTANCE().unloadTasks(setupSession, setupSession.getWorldName());
            //logger.debug("SetupSession removed: " + setupSession.toString());
            setupSession.onStop();
            WorldManager.getINSTANCE().getWorldAdapter().onSetupSessionClose(setupSession);
            if (setupSessions.isEmpty() && setupSessionListener != null) {
                setupSessionListener.unRegister();
                setupSessionListener = null;
            }
        }
    }

    @Override
    public boolean isInSetup(Player player) {
        return setupSessions.stream().anyMatch(session -> session.getPlayer().equals(player));
    }

    @Override
    public boolean isInSetup(CommandSender sender) {
        if (sender instanceof Player) {
            return isInSetup(((Player) sender));
        }
        return false;
    }

    @Override
    public boolean isWorldInUse(String name) {
        return setupSessions.stream().anyMatch(session -> session.getWorldName().equals(name));
    }

    public boolean createSetupSession(Player player, String world) {
        if (isInSetup(player)) return false;
        if (isWorldInUse(world)) return false;
        SetupSession session = new SetupActivity(player, world);
        addSession(session);
        return true;
    }

    public void initializeSavedTasks(SetupSession setupSession, String world) {
        ArenaManager.getINSTANCE().getTemplate(world, false).getProperty(ArenaConfig.TASKS).forEach(task -> {
            String[] taskData = task.split(";");
            if (taskData.length == 4) {
                TaskProvider taskProvider = ArenaManager.getINSTANCE().getTask(taskData[1], taskData[2]);
                if (taskProvider != null) {
                    taskProvider.onSetupLoad(setupSession, taskData[0], new JsonParser().parse(taskData[3]).getAsJsonObject());
                }
            }
        });
    }

    private void unloadTasks(SetupSession setupSession, String world) {
        ArenaManager.getINSTANCE().getTemplate(world, false).getProperty(ArenaConfig.TASKS).forEach(task -> {
            String[] taskData = task.split(";");
            if (taskData.length == 4) {
                TaskProvider taskProvider = ArenaManager.getINSTANCE().getTask(taskData[1], taskData[2]);
                if (taskProvider != null) {
                    taskProvider.onSetupClose(setupSession, taskData[0], new JsonParser().parse(taskData[3]).getAsJsonObject());
                }
            }
        });
    }

    @Override
    public @Nullable SetupSession getSession(String worldName) {
        return setupSessions.stream().filter(session -> session.getWorldName().equals(worldName)).findFirst().orElse(null);
    }

    @Override
    public @Nullable SetupSession getSession(Player player) {
        return setupSessions.stream().filter(session -> session.getPlayer().equals(player)).findFirst().orElse(null);
    }

    @Override
    public FastSubRootCommand getSetCommand() {
        return (FastSubRootCommand) SlaveCommandManager.getINSTANCE().getMainCmd().getSubCommand("set");
    }

    @Override
    public FastSubRootCommand getAddCommand() {
        return (FastSubRootCommand) SlaveCommandManager.getINSTANCE().getMainCmd().getSubCommand("add");
    }

    @Override
    public FastSubRootCommand getRemoveCommand() {
        return (FastSubRootCommand) SlaveCommandManager.getINSTANCE().getMainCmd().getSubCommand("remove");
    }
}
