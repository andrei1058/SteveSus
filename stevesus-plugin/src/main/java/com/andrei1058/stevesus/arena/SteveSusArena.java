package com.andrei1058.stevesus.arena;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.*;
import com.andrei1058.stevesus.api.arena.meeting.ExclusionVoting;
import com.andrei1058.stevesus.api.arena.meeting.MeetingButton;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.room.CuboidRegion;
import com.andrei1058.stevesus.api.arena.room.GameRoom;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageCooldown;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageProvider;
import com.andrei1058.stevesus.api.arena.securitycamera.CamHandler;
import com.andrei1058.stevesus.api.arena.securitycamera.SecurityCam;
import com.andrei1058.stevesus.api.arena.securitycamera.SecurityMonitor;
import com.andrei1058.stevesus.api.arena.task.*;
import com.andrei1058.stevesus.api.arena.team.GameTeamAssigner;
import com.andrei1058.stevesus.api.arena.team.PlayerColorAssigner;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.arena.vent.Vent;
import com.andrei1058.stevesus.api.arena.vent.VentHandler;
import com.andrei1058.stevesus.api.event.*;
import com.andrei1058.stevesus.api.locale.ChatUtil;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.api.server.PlayerCoolDown;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.arena.meeting.MeetingSound;
import com.andrei1058.stevesus.arena.meeting.VoteGUIManager;
import com.andrei1058.stevesus.arena.runnable.ArenaTaskPlaying;
import com.andrei1058.stevesus.arena.runnable.ArenaTaskRestarting;
import com.andrei1058.stevesus.arena.runnable.ArenaTaskStarting;
import com.andrei1058.stevesus.arena.securitycamera.CameraManager;
import com.andrei1058.stevesus.arena.securitycamera.SecurityListener;
import com.andrei1058.stevesus.arena.team.CrewTeam;
import com.andrei1058.stevesus.arena.team.GhostCrewTeam;
import com.andrei1058.stevesus.arena.team.GhostImpostorTeam;
import com.andrei1058.stevesus.arena.team.ImpostorTeam;
import com.andrei1058.stevesus.commanditem.InventoryUtil;
import com.andrei1058.stevesus.commanditem.CommandItemsManager;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.api.selector.ArenaHolderConfig;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.common.party.PartyManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.config.properties.OrphanLocationProperty;
import com.andrei1058.stevesus.hook.corpse.CorpseManager;
import com.andrei1058.stevesus.hook.glowing.GlowingManager;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.prevention.PreventionManager;
import com.andrei1058.stevesus.server.ServerCommonProvider;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.api.server.multiarena.InventoryBackup;
import com.andrei1058.stevesus.sidebar.GameSidebarManager;
import com.andrei1058.stevesus.sidebar.SidebarType;
import com.andrei1058.stevesus.worldmanager.WorldManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class SteveSusArena implements Arena {

    private final String templateWorld;
    private final int gameId;
    private World world;
    private GameState gameState = GameState.LOADING;
    private String spectatePerm;
    private int minPlayers;
    private int maxPlayers;
    private int countdown;
    private String displayName;
    private Instant gameStart;
    private final String gameTag;
    private final ArenaTime mapTime;

    private final LinkedList<Player> players = new LinkedList<>();
    private final LinkedList<Player> spectators = new LinkedList<>();

    private final LinkedList<Location> waitingLocations = new LinkedList<>();
    private final LinkedList<Location> spectatingLocations = new LinkedList<>();
    private final LinkedList<Location> meetingLocations = new LinkedList<>();
    // used for sequential player spawn
    private int currentSpawnPos = 0;
    private int currentSpectatePos = 0;
    private int currentMeetingPos = 0;
    //
    private int gameTask;

    private ItemStack itemWaiting;
    private ItemStack itemStarting;
    private ItemStack itemPlaying;
    private ItemStack itemEnding;

    private final SettingsManager config;

    private final LinkedList<Team> teams = new LinkedList<>();
    private final LinkedList<GameTask> gameTasks = new LinkedList<>();
    private MeetingStage meetingStage = MeetingStage.NO_MEETING;
    private BossBar taskMeterBar;

    private final List<UUID> freeze = new ArrayList<>(); // list of players that cannot move
    private final HashMap<UUID, Integer> meetingsLeft = new HashMap<>();
    private MeetingButton meetingButton;
    private ExclusionVoting currentExclusionVoting;
    private boolean emergency = false;
    private GameEndConditions gameEndConditions;
    private PlayerColorAssigner<?> playerColorAssigner;
    private boolean ignoreColorLimit;
    private final List<PlayerCorpse> deadBodies = new ArrayList<>();
    private final LinkedList<SabotageBase> loadedSabotages = new LinkedList<>();
    private final LinkedList<GameListener> gameListeners = new LinkedList<>();
    private boolean taskIndicatorActive = true;
    private final LinkedList<GameRoom> rooms = new LinkedList<>();
    private GameTeamAssigner teamAssigner = new GameTeamAssigner();

    private VentHandler ventHandler;
    private final HashMap<UUID, InventoryBackup> meetingBackups = new HashMap<>();
    private LiveSettings liveSettings = new LiveSettings();
    private SabotageCooldown sabotageCooldown;
    private CamHandler camHandler;

    public SteveSusArena(String templateWorld, int gameId) {
        this.templateWorld = templateWorld;
        this.gameId = gameId;
        this.gameTag = ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA ? ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_PROXIED_NAME) + ":" : "" + getGameId();
        config = ArenaManager.getINSTANCE().getTemplate(templateWorld, false);

        waitingLocations.addAll(config.getProperty(ArenaConfig.WAITING_LOBBY_LOCATIONS));
        spectatingLocations.addAll(config.getProperty(ArenaConfig.SPECTATE_LOCATIONS));
        meetingLocations.addAll(config.getProperty(ArenaConfig.MEETING_LOCATIONS));
        spectatePerm = config.getProperty(ArenaConfig.SPECTATE_PERM);
        minPlayers = config.getProperty(ArenaConfig.MIN_PLAYERS);
        maxPlayers = config.getProperty(ArenaConfig.MAX_PLAYERS);
        restoreCountDown();
        displayName = config.getProperty(ArenaConfig.DISPLAY_NAME);
        if (displayName.isEmpty()) {
            displayName = getTemplateWorld();
        }

        if (ServerManager.getINSTANCE().getServerType() != ServerType.BUNGEE_LEGACY) {
            itemWaiting = ItemUtil.createItem(config.getProperty(ArenaConfig.SELECTOR_WAITING_MATERIAL), (byte) ((int) config.getProperty(ArenaConfig.SELECTOR_WAITING_DATA)),
                    1, config.getProperty(ArenaConfig.SELECTOR_WAITING_ENCHANT), Arrays.asList(ServerCommonProvider.getInstance().getDisplayableArenaNBTTagKey(), getTag()));
            itemStarting = ItemUtil.createItem(config.getProperty(ArenaConfig.SELECTOR_STARTING_MATERIAL), (byte) ((int) config.getProperty(ArenaConfig.SELECTOR_STARTING_DATA)),
                    1, config.getProperty(ArenaConfig.SELECTOR_STARTING_ENCHANT), Arrays.asList(ServerCommonProvider.getInstance().getDisplayableArenaNBTTagKey(), getTag()));
            itemPlaying = ItemUtil.createItem(config.getProperty(ArenaConfig.SELECTOR_PLAYING_MATERIAL), (byte) ((int) config.getProperty(ArenaConfig.SELECTOR_PLAYING_DATA)),
                    1, config.getProperty(ArenaConfig.SELECTOR_PLAYING_ENCHANT), Arrays.asList(ServerCommonProvider.getInstance().getDisplayableArenaNBTTagKey(), getTag()));
            itemEnding = ItemUtil.createItem(config.getProperty(ArenaConfig.SELECTOR_ENDING_MATERIAL), (byte) ((int) config.getProperty(ArenaConfig.SELECTOR_ENDING_DATA)),
                    1, config.getProperty(ArenaConfig.SELECTOR_ENDING_ENCHANT), Arrays.asList(ServerCommonProvider.getInstance().getDisplayableArenaNBTTagKey(), getTag()));
        }

        this.mapTime = config.getProperty(ArenaConfig.MAP_TIME);
        this.gameEndConditions = ArenaManager.getINSTANCE().getGameEndConditions();
        this.ignoreColorLimit = config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_IGNORE_COLOR_LIMIT);
        this.playerColorAssigner = ArenaManager.getINSTANCE().getDefaultPlayerColorAssigner();
    }

    private void restoreCountDown() {
        countdown = config.getProperty(ArenaConfig.GAME_COUNTDOWN_INITIAL).orElse(ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.GAME_COUNTDOWN_INITIAL));
    }

    @Override
    public void init(World world) {
        this.world = world;
        this.world.setKeepSpawnInMemory(true);
        this.world.setAutoSave(false);
        this.world.getWorldBorder().setSize(1000);
        this.world.getWorldBorder().setWarningDistance(0);
        this.world.setGameRuleValue("announceAdvancements", "false");

        waitingLocations.forEach(location -> location.setWorld(this.world));
        spectatingLocations.forEach(location -> location.setWorld(this.world));
        meetingLocations.forEach(location -> location.setWorld(this.world));

        teams.add(new CrewTeam(this));
        Team impostors = new ImpostorTeam(this);
        teams.add(impostors);
        teams.add(new GhostCrewTeam(this));
        Team ghostImpostors = new GhostImpostorTeam(this);
        teams.add(ghostImpostors);
        sabotageCooldown = new SabotageCooldown(impostors, ghostImpostors, 60);

        if (getTime() != null) {
            world.setTime(getTime().getStartTick());
        }

        initTasks();
        initSabotages();
        initRooms();
        initVents();
        initLiveSettings();

        spawnMeetingButton(world);

        List<SecurityCam> cams = new ArrayList<>();
        List<SecurityMonitor> monitors = new ArrayList<>();
        OrphanLocationProperty importer = new OrphanLocationProperty();
        for (String string : config.getProperty(ArenaConfig.SECURITY_CAMS)) {
            String[] data = string.split(";");
            if (data.length != 2) continue;
            Location location = importer.convert(data[1], null);
            if (location != null) {
                location.setWorld(getWorld());
                //GameRoom room = getRoom(location);
                cams.add(new SecurityCam(location, data[0]));
            }
        }
        for (String string : config.getProperty(ArenaConfig.SECURITY_MONITORS)) {
            Location location = importer.convert(string, null);
            if (location != null) {
                location.setWorld(getWorld());
                //GameRoom room = getRoom(location);
                monitors.add(new SecurityMonitor(location));
            }
        }
        if (!cams.isEmpty()) {
            camHandler = new CameraManager(cams, monitors, this);
        }

        switchState(GameState.WAITING);
    }

    private void initLiveSettings() {
        // load live settings values
        // common tasks
        getLiveSettings().getCommonTasks().setMinValue(Math.max(0, config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_COMMON_MIN)));
        int commonTasks = (int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.COMMON).count();
        if (config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_COMMON_MAX) < 0) {
            getLiveSettings().getCommonTasks().setMaxValue(commonTasks);
        } else {
            getLiveSettings().getCommonTasks().setMaxValue(Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_COMMON_MAX), commonTasks));
        }
        if (config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_COMMON) < 0) {
            getLiveSettings().getCommonTasks().setCurrentValue(getLiveSettings().getCommonTasks().getMaxValue());
        } else {
            getLiveSettings().getCommonTasks().setCurrentValue(Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_COMMON), getLiveSettings().getCommonTasks().getMaxValue()));
        }
        // short tasks
        getLiveSettings().getShortTasks().setMinValue(Math.max(0, config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_SHORT_MIN)));
        int shortTasks = (int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.SHORT).count();
        if (config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_SHORT_MAX) < 0) {
            getLiveSettings().getShortTasks().setMaxValue(shortTasks);
        } else {
            getLiveSettings().getShortTasks().setMaxValue(Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_SHORT_MAX), shortTasks));
        }
        if (config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_SHORT) < 0) {
            getLiveSettings().getShortTasks().setCurrentValue(0);
        } else {
            getLiveSettings().getShortTasks().setCurrentValue(Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_SHORT), getLiveSettings().getShortTasks().getMaxValue()));
        }
        // long tasks
        getLiveSettings().getLongTasks().setMinValue(Math.max(0, config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_LONG_MIN)));
        int longTasks = (int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.LONG).count();
        if (config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_LONG_MAX) < 0) {
            getLiveSettings().getLongTasks().setMaxValue(longTasks);
        } else {
            getLiveSettings().getLongTasks().setMaxValue(Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_LONG_MAX), longTasks));
        }
        if (config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_LONG) < 0) {
            getLiveSettings().getLongTasks().setCurrentValue(getLiveSettings().getLongTasks().getMaxValue());
        } else {
            getLiveSettings().getLongTasks().setCurrentValue(Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_LONG), getLiveSettings().getLongTasks().getMaxValue()));
        }
        //
        getLiveSettings().setVisualTasksEnabled(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_VISUAL_ENABLED));
        getLiveSettings().getMeetingsPerPlayer().setMinValue(Math.min(0, config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_PER_PLAYER_MIN)));
        getLiveSettings().getMeetingsPerPlayer().setMaxValue(Math.max(getLiveSettings().getMeetingsPerPlayer().getMinValue(), config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_PER_PLAYER_MAX)));
        getLiveSettings().getMeetingsPerPlayer().setCurrentValue(Math.min(getLiveSettings().getMeetingsPerPlayer().getMaxValue(), config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_PER_PLAYER)));
        getLiveSettings().getTalkingDuration().setMinValue(Math.min(2, config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_TALK_TIME_MIN)));
        getLiveSettings().getTalkingDuration().setMaxValue(Math.max(getLiveSettings().getTalkingDuration().getMinValue(), config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_TALK_TIME_MAX)));
        getLiveSettings().getTalkingDuration().setCurrentValue(Math.min(getLiveSettings().getTalkingDuration().getMaxValue(), config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_TALK_TIME)));
        getLiveSettings().setVisualTasksEnabled(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_VISUAL_ENABLED));

        getLiveSettings().getVotingDuration().setMinValue(Math.min(2, config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_VOTE_TIME_MIN)));
        getLiveSettings().getVotingDuration().setMaxValue(Math.max(getLiveSettings().getVotingDuration().getMinValue(), config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_VOTE_TIME_MAX)));
        getLiveSettings().getVotingDuration().setCurrentValue(Math.min(getLiveSettings().getVotingDuration().getMaxValue(), config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_VOTE_TIME)));

        getLiveSettings().getEmergencyCoolDown().setMinValue(Math.min(5, config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_COOL_DOWN_MIN)));
        getLiveSettings().getEmergencyCoolDown().setCurrentValue(Math.max(getLiveSettings().getEmergencyCoolDown().getMinValue(), config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_COOL_DOWN)));
        getLiveSettings().getEmergencyCoolDown().setMaxValue(Math.max(getLiveSettings().getEmergencyCoolDown().getMinValue(), config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_COOL_DOWN_MAX)));

        getLiveSettings().getSabotageCooldown().setMinValue(Math.min(5, config.getProperty(ArenaConfig.LIVE_OPTION_SABOTAGE_COOL_DOWN_MIN)));
        getLiveSettings().getSabotageCooldown().setCurrentValue(Math.max(getLiveSettings().getSabotageCooldown().getMinValue(), config.getProperty(ArenaConfig.LIVE_OPTION_SABOTAGE_COOL_DOWN_DEFAULT)));
        getLiveSettings().getSabotageCooldown().setMaxValue(Math.max(getLiveSettings().getSabotageCooldown().getMinValue(), config.getProperty(ArenaConfig.LIVE_OPTION_SABOTAGE_COOL_DOWN_MAX)));

        getLiveSettings().getKillCooldown().setMinValue(Math.min(2, config.getProperty(ArenaConfig.LIVE_OPTION_KILL_COOL_DOWN_MIN)));
        getLiveSettings().getKillCooldown().setCurrentValue(Math.max(getLiveSettings().getKillCooldown().getMinValue(), config.getProperty(ArenaConfig.LIVE_OPTION_KILL_COOL_DOWN_DEFAULT)));
        getLiveSettings().getKillCooldown().setMaxValue(Math.max(getLiveSettings().getKillCooldown().getCurrentValue(), config.getProperty(ArenaConfig.LIVE_OPTION_KILL_COOL_DOWN_MAX)));
        //
        getLiveSettings().init(this);
    }

    private void spawnMeetingButton(World world) {
        Location location = config.getProperty(ArenaConfig.MEETING_BUTTON_LOC).orElse(null);
        if (location != null) {
            location.setWorld(world);
            meetingButton = new MeetingButton(SteveSus.getInstance(), location, this);
        }
    }

    @Override
    public void disable() {
        SteveSus.debug("Disabling game " + getGameId() + "(" + getTemplateWorld() + ").");

        GameDisableEvent gameDisableEvent = new GameDisableEvent(getGameId(), this);
        Bukkit.getPluginManager().callEvent(gameDisableEvent);

        if (world != null) {
            if (ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA) {
                for (Player inWorld : world.getPlayers()) {
                    inWorld.kickPlayer(LanguageManager.getINSTANCE().getMsg(inWorld, CommonMessage.ARENA_STATUS_ENDING_NAME));
                }
            } else {
                for (Player inWorld : world.getPlayers()) {
                    if (isPlayer(inWorld)) {
                        removePlayer(inWorld, false);
                    } else if (isSpectator(inWorld)) {
                        removeSpectator(inWorld, false);
                    } else {
                        inWorld.kickPlayer(LanguageManager.getINSTANCE().getMsg(inWorld, CommonMessage.ARENA_STATUS_ENDING_NAME));
                    }
                }
            }
        }
        if (getPlayerColorAssigner() != null) {
            getPlayerColorAssigner().clearArenaData(this);
        }
        if (gameTask != -1) {
            Bukkit.getScheduler().cancelTask(gameTask);
        }
    }

    @Override
    public void restart() {
        if (gameTask != -1) {
            Bukkit.getScheduler().cancelTask(gameTask);
        }
        if (getPlayerColorAssigner() != null) {
            getPlayerColorAssigner().clearArenaData(this);
        }
        SteveSus.debug("Restarting game " + getGameId() + "(" + getTemplateWorld() + ").");
        ArenaManager.getINSTANCE().removeArena(this);
        GameRestartEvent gameRestartEvent = new GameRestartEvent(getGameId(), this);
        Bukkit.getPluginManager().callEvent(gameRestartEvent);
        WorldManager.getINSTANCE().getWorldAdapter().onArenaRestart(this);
    }

    @Override
    public int getGameId() {
        return gameId;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public String getTemplateWorld() {
        return templateWorld;
    }

    @Override
    public GameState getGameState() {
        return gameState;
    }

    @Override
    public String getDisplayState(@NotNull Player player) {
        return LanguageManager.getINSTANCE().getMsg(player, getGameState().getTranslatePath());
    }

    @Override
    public String getDisplayState(@Nullable Locale language) {
        return language == null ? LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, getGameState().getTranslatePath()) : language.getMsg(null, getGameState().getTranslatePath());
    }

    @Override
    public boolean isFull() {
        return getPlayers().size() >= getMaxPlayers();
    }

    @Override
    public Instant getStartTime() {
        return gameStart;
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public List<Player> getSpectators() {
        return spectators;
    }

    @Override
    public String getSpectatePermission() {
        return spectatePerm;
    }

    @Override
    public void setSpectatePermission(String spectatePermission) {
        this.spectatePerm = spectatePermission;
    }

    @Override
    public boolean addPlayer(Player player, boolean ignoreParty) {
        if (player == null) return false;
        if (ArenaManager.getINSTANCE().isInArena(player)) return false;
        if (getGameState() == GameState.LOADING || getGameState() == GameState.ENDING) return false;

        // Handle party adapter and add members to this game if possible
        if (!ignoreParty) {
            if (PartyManager.getINSTANCE().getPartyAdapter().hasParty(player.getUniqueId())) {
                if (PartyManager.getINSTANCE().getPartyAdapter().isOwner(player.getUniqueId())) {
                    for (UUID member : PartyManager.getINSTANCE().getPartyAdapter().getMembers(player.getUniqueId())) {
                        if (member.equals(player.getUniqueId())) continue;

                        Player playerMember = Bukkit.getPlayer(member);

                        // Add party members to current game if they are in lobby
                        if (playerMember != null && !ArenaManager.getINSTANCE().isInArena(playerMember)) {
                            addPlayer(playerMember, true);
                            playerMember.sendMessage(LanguageManager.getINSTANCE().getMsg(playerMember, CommonMessage.ARENA_JOIN_VIA_PARTY).replace("{arena}", getDisplayName()));
                        }
                    }
                } else {
                    // if did not join the same arena as the party owner
                    UUID partyOwnerUUID = PartyManager.getINSTANCE().getPartyAdapter().getOwner(player.getUniqueId());
                    Player playerPartyOwner = Bukkit.getPlayer(partyOwnerUUID);
                    if (!(partyOwnerUUID == null && playerPartyOwner == null) && !isPlayer(playerPartyOwner)) {
                        // owners only can chose a game
                        player.sendMessage(LanguageManager.getINSTANCE().getMsg(player, CommonMessage.ARENA_JOIN_DENIED_NO_PARTY_LEADER));
                    }
                    return false;
                }
            }
        }

        if (getGameState() == GameState.WAITING || (getGameState() == GameState.STARTING && getCountdown() > 2)) {


            PlayerGameJoinEvent playerGameJoinEvent = new PlayerGameJoinEvent(this, player, false);

            // Handle Vip Join-Kick Feature
            if (isFull()) {
                if (!ArenaManager.getINSTANCE().hasVipJoin(player)) {
                    player.sendMessage(LanguageManager.getINSTANCE().getMsg(player, CommonMessage.ARENA_JOIN_DENIED_GAME_FULL));
                    return false;
                }
                List<Player> canBeKicked = getPlayers().stream().filter(on -> !ArenaManager.getINSTANCE().hasVipJoin(on)).collect(Collectors.toList());

                if (canBeKicked.isEmpty()) {
                    player.sendMessage(LanguageManager.getINSTANCE().getMsg(player, Message.VIP_JOIN_DENIED));
                    return false;
                }

                Bukkit.getPluginManager().callEvent(playerGameJoinEvent);
                if (playerGameJoinEvent.isCancelled()) return false;

                Player removed = canBeKicked.get(0);
                removePlayer(removed, false);
                TextComponent vipKick = new TextComponent(LanguageManager.getINSTANCE().getMsg(removed, Message.VIP_JOIN_KICKED));
                vipKick.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.VIP_JOIN_DETAILS)));
                removed.spigot().sendMessage(vipKick);
            } else {
                Bukkit.getPluginManager().callEvent(playerGameJoinEvent);
                if (playerGameJoinEvent.isCancelled()) return false;
            }

            player.closeInventory();

            if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
                InventoryBackup.createInventoryBackup(player);
            }

            player.teleport(getNextWaitingSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);

            InventoryUtil.wipePlayer(player);
            player.setGameMode(GameMode.SURVIVAL);

            // send items
            CommandItemsManager.sendCommandItems(player, getGameState() == GameState.STARTING ? CommandItemsManager.CATEGORY_STARTING : CommandItemsManager.CATEGORY_WAITING, false);

            // play songs before adding him to the players list so you don't have to filter it. it would be redundant.
            GameSound.JOIN_SOUND_CURRENT.playToPlayers(getPlayers());

            players.add(player);
            ArenaManager.getINSTANCE().setArenaByPlayer(player, this);

            for (Player on : players) {
                on.sendMessage(LanguageManager.getINSTANCE().getMsg(on, Message.ARENA_JOIN_ANNOUNCE).replace("{player}", player.getDisplayName())
                        .replace("{on}", String.valueOf(getPlayers().size())).replace("{max}", String.valueOf(getMaxPlayers())));
            }

            if (getGameState() == GameState.WAITING) {
                // Handle Arena Start
                if (getPlayers().size() >= getMinPlayers()) {
                    // if changing state this will give him a scoreboard as well
                    switchState(GameState.STARTING);
                } else {
                    // else give scoreboard
                    GameSidebarManager.getInstance().setSidebar(player, SidebarType.WAITING, this, ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA);
                }
            }

            if (getGameState() == GameState.STARTING) {
                // Give scoreboard
                GameSidebarManager.getInstance().setSidebar(player, SidebarType.STARTING, this, ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA);
                // Handle Time Shortener
                if (getPlayers().size() >= getMaxPlayers()) {
                    int shortenedTime = config.getProperty(ArenaConfig.GAME_COUNTDOWN_SHORTENED).orElse(ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.GAME_COUNTDOWN_SHORTENED));
                    if (shortenedTime < 2) shortenedTime = 2;
                    if (getCountdown() > shortenedTime) {
                        setCountdown(shortenedTime);
                    }
                }
            }

            // hide to player outside arena
            // show players and vice-versa
            SteveSus.newChain().delay(17).sync(() -> {
                GameSound.JOIN_SOUND_SELF.playToPlayer(player);
                for (Player onServer : Bukkit.getOnlinePlayers()) {
                    if (onServer.equals(player)) continue;
                    if (isPlayer(onServer)) {
                        onServer.showPlayer(SteveSus.getInstance(), player);
                        player.showPlayer(SteveSus.getInstance(), onServer);
                    } else {
                        onServer.hidePlayer(SteveSus.getInstance(), player);
                        player.hidePlayer(SteveSus.getInstance(), onServer);

                    }

                }
            }).execute();

            // clear cached cool downs
            PlayerCoolDown.clearPlayerData(player);

            // trigger listener
            for (GameListener gameListener : gameListeners) {
                gameListener.onPlayerJoin(this, player);
            }

            // remove existing glowing on players
            for (Player inGame : getPlayers()) {
                GlowingManager.getInstance().removeGlowing(inGame, player);
                GlowingManager.getInstance().removeGlowing(player, inGame);
            }

            SteveSus.debug("Player " + player.getName() + " was added as player to game " + getGameId() + "(" + getTemplateWorld() + ").");
            return true;
        }
        return false;
    }

    @Override
    public boolean addSpectator(Player player, @Nullable Location target) {
        if (!(getSpectatePermission().trim().isEmpty() || player.hasPermission(getSpectatePermission()))) return false;
        if (ArenaManager.getINSTANCE().isInArena(player)) return false;
        if (getGameState() == GameState.LOADING || getGameState() == GameState.ENDING) return false;
        PlayerGameJoinEvent playerGameJoinEvent = new PlayerGameJoinEvent(this, player, true);
        Bukkit.getPluginManager().callEvent(playerGameJoinEvent);
        if (playerGameJoinEvent.isCancelled()) return false;

        player.closeInventory();

        if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
            InventoryBackup.createInventoryBackup(player);
        }

        Team team = getPlayerTeam(player);
        if (team != null) {
            team.removePlayer(player);
        }

        if (target != null) {
            target.setWorld(getWorld());
        }
        player.teleport(target == null ? getNextSpectatorSpawn() : target, PlayerTeleportEvent.TeleportCause.PLUGIN);

        InventoryUtil.wipePlayer(player);

        // send items
        CommandItemsManager.sendCommandItems(player, CommandItemsManager.CATEGORY_SPECTATING);
        // give scoreboard
        GameSidebarManager.getInstance().setSidebar(player, SidebarType.SPECTATOR, this, ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA);

        spectators.add(player);
        ArenaManager.getINSTANCE().setArenaByPlayer(player, this);
        player.setGameMode(GameMode.SURVIVAL);

        SteveSus.newChain().delay(5).sync(() -> {
            player.setAllowFlight(true);
            player.setFlying(true);

            GameSound.JOIN_SPECTATOR_SOUND_SELF.playToPlayer(player);
        }).delay(12).sync(() -> {
            for (Player onServer : Bukkit.getOnlinePlayers()) {
                if (isPlayer(onServer)) {
                    player.showPlayer(SteveSus.getInstance(), onServer);
                } else if (isSpectator(onServer)) {
                    player.showPlayer(SteveSus.getInstance(), onServer);
                    onServer.showPlayer(SteveSus.getInstance(), player);
                } else {
                    player.hidePlayer(SteveSus.getInstance(), onServer);
                    onServer.hidePlayer(SteveSus.getInstance(), player);
                }
            }
        }).execute();

        player.sendMessage(LanguageManager.getINSTANCE().getMsg(player, Message.ARENA_JOIN_SPECTATOR).replace("{arena}", this.getDisplayName()));

        sendTaskMeter(player);
        PlayerCoolDown.clearPlayerData(player);
        SteveSus.debug("Player " + player.getName() + " was added as spectator to game " + getGameId() + "(" + getTemplateWorld() + ").");
        return true;
    }

    @Override
    public boolean switchToSpectator(Player player) {
        if (!isPlayer(player)) return false;
        if (getCamHandler() != null){
            if (getCamHandler().isOnCam(player, this)){
                getCamHandler().stopWatching(player, this);
            }
        }
        players.remove(player);
        spectators.add(player);
        // clear countdown cache
        PlayerCoolDown.clearPlayerData(player);
        // clear glowing
        getPlayers().forEach(inGame -> GlowingManager.getInstance().removeGlowing(player, inGame));
        for (Entity entity : player.getPassengers()) {
            player.removePassenger(entity);
        }

        Team team = getPlayerTeam(player);
        if (team != null) {
            team.removePlayer(player);
        }

        // call event
        PlayerToSpectatorEvent playerToSpectatorEvent = new PlayerToSpectatorEvent(this, player);
        Bukkit.getPluginManager().callEvent(playerToSpectatorEvent);

        if (getVentHandler() != null) {
            getVentHandler().interruptVenting(player, true);
        }

        player.closeInventory();
        // tp to spectator spawn
        player.teleport(getNextSpectatorSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        // clear inv
        InventoryUtil.wipePlayer(player);
        // send items
        CommandItemsManager.sendCommandItems(player, CommandItemsManager.CATEGORY_SPECTATING);
        // change gm
        player.setGameMode(GameMode.SURVIVAL);
        // give scoreboard
        GameSidebarManager.getInstance().setSidebar(player, SidebarType.SPECTATOR, this, ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA);

        SteveSus.newChain().delay(5).sync(() -> {
            // give fly
            player.setAllowFlight(true);
            player.setFlying(true);
        }).delay(12).sync(() -> {
            // hide
            for (Player onServer : Bukkit.getOnlinePlayers()) {
                if (isPlayer(onServer)) {
                    player.showPlayer(SteveSus.getInstance(), onServer);
                } else if (isSpectator(onServer)) {
                    player.showPlayer(SteveSus.getInstance(), onServer);
                    onServer.showPlayer(SteveSus.getInstance(), player);
                } else {
                    player.hidePlayer(SteveSus.getInstance(), onServer);
                    onServer.hidePlayer(SteveSus.getInstance(), player);
                }
            }
        }).execute();

        // trigger listener
        for (GameListener gameListener : gameListeners) {
            gameListener.onPlayerToSpectator(this, player);
        }

        SteveSus.debug("Player " + player.getName() + " was SWITCHED to spectator in game " + getGameId() + "(" + getTemplateWorld() + ").");
        return true;
    }

    @Override
    public int getCountdown() {
        return countdown;
    }

    @Override
    public boolean isPlayer(Player player) {
        return players.contains(player);
    }

    @Override
    public boolean isSpectator(Player player) {
        return spectators.contains(player);
    }

    @Override
    public void removePlayer(Player player, boolean onQuit) {
        if (!isPlayer(player)) return;

        PlayerGameLeaveEvent playerGameLeaveEvent = new PlayerGameLeaveEvent(this, player, false, PreventionManager.getInstance().hasAbandoned(this, player));
        Bukkit.getPluginManager().callEvent(playerGameLeaveEvent);

        for (Entity entity : player.getPassengers()) {
            player.removePassenger(entity);
        }

        if (getCamHandler() != null){
            if (getCamHandler().isOnCam(player, this)){
                getCamHandler().stopWatching(player, this);
            }
        }

        players.remove(player);
        ArenaManager.getINSTANCE().setArenaByPlayer(player, null);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        InventoryUtil.wipePlayer(player);

        if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
            // player should be teleported even on disconnect so it teleport it to the place where he left at join and then back to spawn
            Location mainLobby = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.MULTI_ARENA_SPAWN_LOC);
            player.teleport((mainLobby == null || mainLobby.getWorld() == null) ? Bukkit.getWorlds().get(0).getSpawnLocation() : mainLobby, PlayerTeleportEvent.TeleportCause.PLUGIN);
            // send scoreboard
            GameSidebarManager.getInstance().setSidebar(player, SidebarType.MULTI_ARENA_LOBBY, this, false);

            if (!onQuit) {
                InventoryBackup.restoreInventory(player);
                SteveSus.newChain().sync(() -> {
                    for (Player inLobby : player.getWorld().getPlayers()) {
                        if (inLobby.equals(player)) continue;
                        if (!ArenaManager.getINSTANCE().isInArena(inLobby)) {
                            inLobby.showPlayer(SteveSus.getInstance(), player);
                            player.showPlayer(SteveSus.getInstance(), inLobby);
                        }
                    }
                }).delay(17).execute();
            } else {
                InventoryBackup.dropOnQuit(player.getUniqueId());
            }
        } else {
            if (!onQuit) {
                ServerManager.getINSTANCE().getDisconnectHandler().performDisconnect(player);
            }
        }

        if (getGameState() == GameState.STARTING) {
            if (getPlayers().size() < getMinPlayers()) {
                switchState(GameState.WAITING);
                GameSound.LEAVE_SOUND_START_INTERRUPTED_CURRENT.playToPlayers(getPlayers());
                for (Player on : players) {
                    Locale playerLocale = LanguageManager.getINSTANCE().getLocale(on);
                    on.sendMessage(playerLocale.getMsg(on, Message.COUNTDOWN_START_CANCELLED));
                    String title = playerLocale.getMsg(on, Message.TITLE_COUNTDOWN_STOPPED);
                    String subTitle = playerLocale.getMsg(on, Message.SUBTITLE_COUNTDOWN_STOPPED);
                    if (title.isEmpty()) title = " ";
                    if (subTitle.isEmpty()) subTitle = " ";
                    on.sendTitle(title, subTitle, 10, 45, 10);
                }
            } else {
                // send regular leave sound
                GameSound.LEAVE_SOUND_CURRENT.playToPlayers(getPlayers());
            }
        } else {
            // send regular leave sound
            GameSound.LEAVE_SOUND_CURRENT.playToPlayers(getPlayers());
        }

        PreventionManager.getInstance().hasAbandoned(this, player);

        Team team = getPlayerTeam(player);
        if (team != null) {
            team.removePlayer(player);
        }

        // if in game status, will check if it is the case to end this game
        getGameEndConditions().tickGameEndConditions(this);

        //if (getGameState() != GameState.ENDING) {
        for (Player inArena : getPlayers()) {
            inArena.sendMessage(LanguageManager.getINSTANCE().getMsg(inArena, Message.LEAVE_ANNOUNCE).replace("{player}", player.getDisplayName())
                    .replace("{on}", String.valueOf(getPlayers().size())).replace("{max}", String.valueOf(getMaxPlayers())));
        }

        for (Player inArena : getSpectators()) {
            inArena.sendMessage(LanguageManager.getINSTANCE().getMsg(inArena, Message.LEAVE_ANNOUNCE).replace("{player}", player.getDisplayName())
                    .replace("{on}", String.valueOf(getPlayers().size())).replace("{max}", String.valueOf(getMaxPlayers())));
        }
        //}
        // enable movement if disabled
        setCantMove(player, false);
        meetingsLeft.remove(player.getUniqueId());
        // remove boss bar
        removeTaskMeter(player);
        // remove player related color
        if (getPlayerColorAssigner() != null) {
            getPlayerColorAssigner().restorePlayer(player);
        }
        // clear count down cache
        PlayerCoolDown.clearPlayerData(player);
        // remove glowing
        for (Player inGame : players) {
            GlowingManager.getInstance().removeGlowing(player, inGame);
        }

        // trigger listener
        for (GameListener gameListener : gameListeners) {
            gameListener.onPlayerLeave(this, player, false);
        }

        if (getVentHandler() != null) {
            getVentHandler().interruptVenting(player, true);
        }

        SteveSus.debug("Player " + player.getName() + " was removed as player from game " + getGameId() + "(" + getTemplateWorld() + ").");
    }

    @Override
    public void removeSpectator(Player player, boolean onQuit) {
        if (!isSpectator(player)) return;

        PlayerGameLeaveEvent playerGameLeaveEvent = new PlayerGameLeaveEvent(this, player, true, false);
        Bukkit.getPluginManager().callEvent(playerGameLeaveEvent);

        spectators.remove(player);
        ArenaManager.getINSTANCE().setArenaByPlayer(player, null);

        InventoryUtil.wipePlayer(player);

        if (ServerManager.getINSTANCE().getServerType() == ServerType.MULTI_ARENA) {
            // player should be teleported even on disconnect so it teleport it to the place where he left at join and then back to spawn
            Location mainLobby = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.MULTI_ARENA_SPAWN_LOC);
            player.teleport((mainLobby == null || mainLobby.getWorld() == null) ? Bukkit.getWorlds().get(0).getSpawnLocation() : mainLobby, PlayerTeleportEvent.TeleportCause.PLUGIN);

            if (!onQuit) {
                InventoryBackup.restoreInventory(player);
                SteveSus.newChain().sync(() -> {
                    for (Player inLobby : player.getWorld().getPlayers()) {
                        if (inLobby.equals(player)) continue;
                        if (!ArenaManager.getINSTANCE().isInArena(inLobby)) {
                            inLobby.showPlayer(SteveSus.getInstance(), player);
                            player.showPlayer(SteveSus.getInstance(), inLobby);
                        }
                    }
                }).delay(17).execute();
                player.setFlying(false);
                player.setAllowFlight(false);
            } else {
                InventoryBackup.dropOnQuit(player.getUniqueId());
            }
        } else {
            if (!onQuit) {
                ServerManager.getINSTANCE().getDisconnectHandler().performDisconnect(player);
            }
        }
        setCantMove(player, false);
        meetingsLeft.remove(player.getUniqueId());
        removeTaskMeter(player);
        if (getPlayerColorAssigner() != null) {
            getPlayerColorAssigner().restorePlayer(player);
        }
        PlayerCoolDown.clearPlayerData(player);

        // clear player data from sabotages
        // trigger listener
        for (GameListener gameListener : gameListeners) {
            gameListener.onPlayerLeave(this, player, true);
        }

        SteveSus.debug("Player " + player.getName() + " was removed as spectator from game " + getGameId() + "(" + getTemplateWorld() + ").");
    }

    @Override
    public boolean switchState(GameState gameState) {
        if (getGameState() == gameState) return false;

        if (gameTask != -1) {
            Bukkit.getScheduler().cancelTask(gameTask);
            gameTask = -1;
        }

        GameState oldState = this.gameState;
        this.gameState = gameState;

        if (gameState == GameState.WAITING) {
            restoreCountDown();
            getPlayers().forEach(player -> GameSidebarManager.getInstance().setSidebar(player, SidebarType.WAITING, this, false));
        } else if (gameState == GameState.STARTING) {
            getPlayers().forEach(player -> GameSidebarManager.getInstance().setSidebar(player, SidebarType.STARTING, this, false));
            gameTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), new ArenaTaskStarting(this), 0L, 20L).getTaskId();
        } else if (gameState == GameState.IN_GAME) {
            GameSidebarManager.hidePlayerNames(this);
            if (getLiveSettings().getTaskMeterUpdatePolicy() != TaskMeterUpdatePolicy.NEVER) {
                createTaskMeterBar();
            }
            gameStart = Instant.now();
            for (Player player : getPlayers()) {
                player.getInventory().clear();
                player.teleport(getNextMeetingSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                meetingsLeft.putIfAbsent(player.getUniqueId(), getLiveSettings().getMeetingsPerPlayer().getCurrentValue());
            }
            gameTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), new ArenaTaskPlaying(this), 0L, 20L).getTaskId();
            // assign teams
            Collections.shuffle(players);
            teamAssigner.assignTeams(this);
            // apply start cool down to sabotages
            if (getSabotageCooldown() != null) {
                getSabotageCooldown().applyStartCooldown();
            }
            // assign colors AFTER teams
            if (getPlayerColorAssigner() != null) {
                Collections.shuffle(players);
                for (Player player : players) {
                    PlayerColorAssigner.PlayerColor playerColor = getPlayerColorAssigner().assignPlayerColor(player, this, isIgnoreColorLimit());
                    if (playerColor == null) {
                        throw new IllegalStateException("Could not assign a color to: " + player.getName() + "! Player amount was greater than available colors. To avoid this issue change arena's player limit or set " + ArenaConfig.DEFAULT_GAME_OPTION_IGNORE_COLOR_LIMIT.getPath() + " to true.");
                    }
                    playerColor.apply(player, this);
                }
            }
            // assign tasks
            GameTaskAssigner gameTaskAssigner = new GameTaskAssigner(this);
            teams.forEach(gameTaskAssigner::assignTasks);
            // change sidebar
            getPlayers().forEach(player -> GameSidebarManager.getInstance().setSidebar(player, SidebarType.IN_GAME, this, false));
            // refresh meeting button lines
            if (getMeetingButton() != null) {
                getMeetingButton().onGameStart();
                getMeetingButton().refreshLines(this);
                getMeetingButton().setLastUsage(System.currentTimeMillis());
            }

        } else if (gameState == GameState.ENDING) {
            setMeetingStage(MeetingStage.NO_MEETING);
            for (Player player : getPlayers()) {
                GameSidebarManager.getInstance().setSidebar(player, SidebarType.ENDING, this, false);
            }
            for (Player player : getSpectators()) {
                GameSidebarManager.getInstance().setSidebar(player, SidebarType.ENDING, this, false);
            }
            gameTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), new ArenaTaskRestarting(this), 0L, 20L).getTaskId();
        }

        for (GameListener listener : getGameListeners()) {
            listener.onGameStateChange(this, oldState, gameState);
        }

        GameStateChangeEvent gameStateChangeEvent = new GameStateChangeEvent(this, oldState, gameState);
        Bukkit.getPluginManager().callEvent(gameStateChangeEvent);
        return true;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        if (displayName == null) {
            this.displayName = getTemplateWorld();
            return;
        }
        if (!displayName.isEmpty()) {
            this.displayName = displayName;
        }
    }

    @Override
    public int getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        if (maxPlayers > 1) {
            this.maxPlayers = maxPlayers;
        }
    }

    @Override
    public int getMinPlayers() {
        return minPlayers;
    }

    @Override
    public int getCurrentPlayers() {
        return getPlayers().size();
    }

    @Override
    public int getCurrentSpectators() {
        return getSpectators().size();
    }

    @Override
    public String getTag() {
        return gameTag;
    }

    @Override
    public void setMinPlayers(int minPlayers) {
        if (minPlayers > 1) {
            this.minPlayers = minPlayers;
        }
    }

    @Override
    public void setCountdown(int seconds) {
        if (seconds > 0) {
            this.countdown = seconds;
        }
    }

    @Override
    public boolean canForceStart() {
        return players.size() >= 4;
    }

    @Override
    public void startFirstPersonSpectate(Player spectator, Player target) {
        if (!isSpectator(spectator)) return;
        if (!isPlayer(target)) return;
        if (isFirstPersonSpectate(spectator)) return;
        SpectatorFirstPersonEvent spectatorFirstPersonEvent = new SpectatorFirstPersonEvent(this, spectator, target, SpectatorFirstPersonEvent.SpectateAction.START);
        Bukkit.getPluginManager().callEvent(spectatorFirstPersonEvent);
        if (spectatorFirstPersonEvent.isCancelled()) return;

        spectator.setGameMode(GameMode.SPECTATOR);
        spectator.getInventory().setHeldItemSlot(5);
        spectator.setSpectatorTarget(target);

        String title = LanguageManager.getINSTANCE().getMsg(spectator, Message.TITLE_SPECTATE_FIRST_PERSON_START).replace("{target}", target.getDisplayName()).replace("{target_raw}", target.getName());
        String subTitle = LanguageManager.getINSTANCE().getMsg(spectator, Message.SUBTITLE_SPECTATE_FIRST_PERSON_START).replace("{target}", target.getDisplayName()).replace("{target_raw}", target.getName());
        if (title.isEmpty()) {
            title = " ";
        }
        if (subTitle.isEmpty()) {
            subTitle = " ";
        }
        spectator.sendTitle(title, subTitle, 0, 50, 0);
    }

    @Override
    public void stopFirstPersonSpectate(Player spectator) {
        if (!isFirstPersonSpectate(spectator)) return;
        Player target = (Player) spectator.getSpectatorTarget();
        spectator.setSpectatorTarget(null);
        spectator.setGameMode(GameMode.SURVIVAL);
        spectator.setAllowFlight(true);
        spectator.setFlying(true);


        SpectatorFirstPersonEvent spectatorFirstPersonEvent = new SpectatorFirstPersonEvent(this, spectator, target, SpectatorFirstPersonEvent.SpectateAction.STOP);
        Bukkit.getPluginManager().callEvent(spectatorFirstPersonEvent);

        String title = LanguageManager.getINSTANCE().getMsg(spectator, Message.TITLE_SPECTATE_FIRST_PERSON_STOP).replace("{target}", target.getDisplayName()).replace("{target_raw}", target.getName());
        String subTitle = LanguageManager.getINSTANCE().getMsg(spectator, Message.SUBTITLE_SPECTATE_FIRST_PERSON_STOP).replace("{target}", target.getDisplayName()).replace("{target_raw}", target.getName());
        if (title.isEmpty()) {
            title = " ";
        }
        if (subTitle.isEmpty()) {
            subTitle = " ";
        }
        spectator.sendTitle(title, subTitle, 0, 40, 0);
    }

    @Override
    public boolean isFirstPersonSpectate(Player spectator) {
        return isSpectator(spectator) && spectator.getSpectatorTarget() != null && spectator.getGameMode() == GameMode.SPECTATOR;
    }

    @Override
    public @Nullable ArenaTime getTime() {
        return mapTime;
    }

    @Override
    public LinkedList<GameTask> getLoadedGameTasks() {
        return gameTasks;
    }

    @Override
    public Team getPlayerTeam(Player player) {
        return teams.stream().filter(team -> team.isMember(player)).findFirst().orElse(null);
    }

    @Override
    public Team getTeamByName(String name) {
        return teams.stream().filter(team -> team.getIdentifier().equals(name)).findFirst().orElse(null);
    }

    @Override
    public List<Team> getGameTeams() {
        return teams;
    }

    @Override
    public void setCantMove(Player player, boolean toggle) {
        if (toggle) {
            if (!freeze.contains(player.getUniqueId())) {
                freeze.add(player.getUniqueId());
            }
        } else {
            freeze.remove(player.getUniqueId());
        }
    }

    @Override
    public boolean isCantMove(Player player) {
        return freeze.contains(player.getUniqueId());
    }

    @Override
    public void refreshTaskMeter() {
        if (getLiveSettings().getTaskMeterUpdatePolicy() == TaskMeterUpdatePolicy.NEVER) {
            if (this.taskMeterBar != null) {
                this.taskMeterBar.removeAll();
            }
            return;
        }
        if (this.taskMeterBar == null) {
            if (getGameState() == GameState.IN_GAME) {
                createTaskMeterBar();
            }
            return;
        }
        if (getLiveSettings().getTaskMeterUpdatePolicy() == TaskMeterUpdatePolicy.MEETINGS && getMeetingStage() == MeetingStage.NO_MEETING) {
            return;
        }
        final double[] assignedTasks = {0};
        final double[] completedTasks = {0};
        getLoadedGameTasks().forEach(task -> {
            if (!task.getAssignedPlayers().isEmpty()) {
                task.getAssignedPlayers().forEach(assignedPlayer -> {
                    assignedTasks[0]++;
                    if (task.getCurrentStage(assignedPlayer) == task.getTotalStages(assignedPlayer)) {
                        completedTasks[0]++;
                    }
                });
            }
        });
        taskMeterBar.setProgress(completedTasks[0] / assignedTasks[0]);
    }

    @Override
    public MeetingStage getMeetingStage() {
        return meetingStage;
    }

    @Override
    public void setMeetingStage(MeetingStage meetingStage) {
        if (meetingStage == this.meetingStage) return;
        MeetingStage oldStage = this.meetingStage;
        this.meetingStage = meetingStage;
        if (meetingStage == MeetingStage.NO_MEETING) {
            if (getMeetingButton() != null) {
                getMeetingButton().refreshLines(this);
                getMeetingButton().setLastUsage(System.currentTimeMillis());
            }
            for (Player player : players) {
                setCantMove(player, false);
                player.closeInventory();
                InventoryUtil.clearStorageContents(player);
                InventoryBackup backup = meetingBackups.remove(player.getUniqueId());
                if (backup != null) {
                    backup.restore(player);
                }
            }
            getGameEndConditions().tickGameEndConditions(this);
        } else if (meetingStage == MeetingStage.TALKING) {
            setCountdown(getLiveSettings().getTalkingDuration().getCurrentValue());
            for (Player player : players) {
                player.closeInventory();
                if (!meetingBackups.containsKey(player.getUniqueId())) {
                    meetingBackups.put(player.getUniqueId(), new InventoryBackup(player));
                }
                InventoryUtil.clearStorageContents(player);
                setCantMove(player, true);
            }
        } else if (meetingStage == MeetingStage.VOTING) {
            setCountdown(getLiveSettings().getVotingDuration().getCurrentValue());
            for (Player player : players) {
                setCantMove(player, true);
                if (!meetingBackups.containsKey(player.getUniqueId())) {
                    meetingBackups.put(player.getUniqueId(), new InventoryBackup(player));
                }
                VoteGUIManager.openToPlayer(player, this);
                CommandItemsManager.sendCommandItems(player, CommandItemsManager.CATEGORY_VOTING, false);
            }
        } else if (meetingStage == MeetingStage.EXCLUSION_SCREEN) {
            setCountdown(5);
            for (Player player : players) {
                setCantMove(player, false);
                player.closeInventory();
                InventoryUtil.clearStorageContents(player);
            }
            if (currentExclusionVoting != null) {
                currentExclusionVoting.performExclusion(this, null);
                setCurrentVoting(null);
            }
        }
        for (GameListener listener : getGameListeners()) {
            listener.onMeetingStageChange(this, oldStage, meetingStage);
        }
        GameMeetingStageChangeEvent event = new GameMeetingStageChangeEvent(this, oldStage, getMeetingStage());
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public boolean startMeeting(Player requester, @Nullable Player deadBody) {
        if (getGameState() != GameState.IN_GAME) return false;
        if (getMeetingStage() != MeetingStage.NO_MEETING) return false;
        Team playerTeam = getPlayerTeam(requester);
        if (playerTeam == null) return false;
        if (deadBody == null) {
            if (!playerTeam.canUseMeetingButton() || getLoadedSabotages().stream().anyMatch(sabotage -> sabotage.isActive() && !sabotage.isEmergencyButtonAllowed())) {
                return false;
            }
        } else {
            if (!playerTeam.canReportBody() || getLoadedSabotages().stream().anyMatch(sabotage -> sabotage.isActive() && !sabotage.canReportDeadBody())) {
                return false;
            }
        }
        // interrupt venting
        if (getVentHandler() != null) {
            for (Player player : getPlayers()) {
                getVentHandler().interruptVenting(player, false);
            }
        }
        // interrupt tasks
        interruptTasks();
        // clear dead bodies
        // show new ghosts in tab
        for (PlayerCorpse corpse : getDeadBodies()) {
            Player ghost = Bukkit.getPlayer(corpse.getOwner());
            if (ghost != null && isPlayer(ghost)) {
                GameSidebarManager.spoilGhostInTab(ghost, this);
            }
            corpse.destroy();
        }
        // add voting manager
        setCurrentVoting(new ExclusionVoting());
        // clear glowing
        getGameTeams().forEach(team -> {
            if (!team.isInnocent()) {
                getPlayers().forEach(inGame -> team.getMembers().forEach(member -> GlowingManager.getInstance().removeGlowing(inGame, member)));
            }
        });
        //
        if (deadBody == null) {
            if (getMeetingsLeft(requester) <= 0) return false;
            getPlayers().forEach(player -> player.teleport(getNextMeetingSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN));
            setMeetingStage(MeetingStage.TALKING);

            int meetings = meetingsLeft.remove(requester.getUniqueId());
            meetings--;
            if (meetings > 0) {
                meetingsLeft.put(requester.getUniqueId(), meetings);
            }
            GameSound.EMERGENCY_MEETING.playToPlayers(getPlayers());
            GameSound.EMERGENCY_MEETING.playToPlayers(getSpectators());
            getPlayers().forEach(player -> {
                Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                lang.getMsgList(player, Message.MEETING_START_CHAT_MSG_NO_BODY.toString(), new String[]{"{requester}", requester.getDisplayName()}).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                player.sendTitle(lang.getMsg(player, Message.EMERGENCY_MEETING_TITLE).replace("{player}", requester.getDisplayName()), lang.getMsg(player, Message.EMERGENCY_MEETING_SUBTITLE).replace("{player}", requester.getDisplayName()), 0, 80, 0);
            });
            getSpectators().forEach(player -> {
                Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                lang.getMsgList(player, Message.MEETING_START_CHAT_MSG_NO_BODY.toString(), new String[]{"{requester}", requester.getDisplayName()}).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                player.sendTitle(lang.getMsg(player, Message.EMERGENCY_MEETING_TITLE).replace("{player}", requester.getDisplayName()), lang.getMsg(player, Message.EMERGENCY_MEETING_SUBTITLE).replace("{player}", requester.getDisplayName()), 0, 80, 0);
            });
        } else {
            getPlayers().forEach(player -> player.teleport(getNextMeetingSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN));
            setMeetingStage(MeetingStage.TALKING);
            MeetingSound.playMusic(this, 38);
            getPlayers().forEach(player -> {
                Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                lang.getMsgList(player, Message.MEETING_START_CHAT_MSG_BODY.toString(), new String[]{"{reporter}", requester.getDisplayName(), "{dead}", deadBody.getDisplayName()}).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                player.sendTitle(lang.getMsg(player, Message.EMERGENCY_MEETING_DEAD_TITLE).replace("{reporter}", requester.getDisplayName()).replace("{dead}", deadBody.getDisplayName()), lang.getMsg(player, Message.EMERGENCY_MEETING_DEAD_SUBTITLE).replace("{reporter}", requester.getDisplayName()).replace("{dead}", deadBody.getDisplayName()), 0, 80, 0);
                player.playEffect(EntityEffect.TOTEM_RESURRECT);
            });
            getSpectators().forEach(player -> {
                Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                lang.getMsgList(player, Message.MEETING_START_CHAT_MSG_BODY.toString(), new String[]{"{reporter}", requester.getDisplayName(), "{dead}", deadBody.getDisplayName()}).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                player.sendTitle(lang.getMsg(player, Message.EMERGENCY_MEETING_DEAD_TITLE).replace("{reporter}", requester.getDisplayName()).replace("{dead}", deadBody.getDisplayName()), lang.getMsg(player, Message.EMERGENCY_MEETING_DEAD_SUBTITLE).replace("{reporter}", requester.getDisplayName()).replace("{dead}", deadBody.getDisplayName()), 0, 80, 0);
            });
        }
        return true;
    }

    @Override
    public int getMeetingsLeft(Player player) {
        return meetingsLeft.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public @Nullable MeetingButton getMeetingButton() {
        return meetingButton;
    }

    @Override
    public void setMeetingsLeft(Player player, int amount) {
        if (!isPlayer(player)) return;
        meetingsLeft.remove(player.getUniqueId());
        meetingsLeft.put(player.getUniqueId(), amount);
    }

    @Override
    public @Nullable ExclusionVoting getCurrentVoting() {
        return currentExclusionVoting;
    }

    @Override
    public void setCurrentVoting(@Nullable ExclusionVoting exclusionVoting) {
        this.currentExclusionVoting = exclusionVoting;
    }

    @Override
    public void setEmergency(boolean toggle) {
        if (toggle == this.emergency) return;
        this.emergency = toggle;
    }

    @Override
    public void setGameEndConditions(@NotNull GameEndConditions gameEndConditions) {
        this.gameEndConditions = gameEndConditions;
    }

    @Override
    public @NotNull GameEndConditions getGameEndConditions() {
        return gameEndConditions;
    }

    @Override
    public @Nullable PlayerColorAssigner<?> getPlayerColorAssigner() {
        return playerColorAssigner;
    }

    @Override
    public void setPlayerColorAssigner(@Nullable PlayerColorAssigner<?> playerColorAssigner) {
        if (getPlayerColorAssigner() != null) {
            getPlayers().forEach(player -> getPlayerColorAssigner().restorePlayer(player));
        }
        this.playerColorAssigner = playerColorAssigner;
    }

    @Override
    public void setIgnoreColorLimit(boolean toggle) {
        this.ignoreColorLimit = toggle;
    }

    @Override
    public boolean isIgnoreColorLimit() {
        return ignoreColorLimit;
    }

    @Override
    public List<PlayerCorpse> getDeadBodies() {
        return Collections.unmodifiableList(deadBodies);
    }

    @Override
    public void addDeadBody(PlayerCorpse playerCorpse) {
        removeDeadBody(playerCorpse);
        deadBodies.add(playerCorpse);
    }

    @Override
    public void removeDeadBody(PlayerCorpse playerCorpse) {
        PlayerCorpse oldCorpse = getDeadBody(playerCorpse.getOwner());
        if (oldCorpse != null) {
            deadBodies.remove(oldCorpse);
            oldCorpse.destroy();
        }
    }

    @Override
    public @Nullable PlayerCorpse getDeadBody(UUID playerOwner) {
        return deadBodies.stream().filter(body -> body.getOwner().equals(playerOwner)).findFirst().orElse(null);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void killPlayer(@NotNull Player killer, @NotNull Player victim) {
        if (getGameState() != GameState.IN_GAME) return;
        Team killerTeam = getPlayerTeam(killer);
        if (killerTeam == null) return;
        if (!killerTeam.canKill(victim)) return;
        if (getCamHandler() != null && getCamHandler().isOnCam(killer, this)){
            return;
        }
        Team victimTeam = getPlayerTeam(victim);
        Team destinationTeam = victimTeam == null ? null : getTeamByName(victimTeam.getIdentifier() + "-ghost");
        PlayerKillEvent event = new PlayerKillEvent(this, killer, victim, destinationTeam);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        // interrupt tasks
        interruptTasks(victim);

        // remove from cams
        if (getCamHandler() != null) {
            if (getCamHandler().isOnCam(victim, this)) {
                getCamHandler().stopWatching(victim, this);
            }
        }

        GameSound.KILL.playAtLocation(victim.getLocation(), getPlayers());

        // remove glowing
        GlowingManager.getInstance().removeGlowing(victim, killer);

        ItemStack helmet = victim.getInventory().getHelmet();
        ItemStack chestPlate = victim.getInventory().getChestplate();
        ItemStack leggings = victim.getInventory().getLeggings();
        ItemStack boots = victim.getInventory().getBoots();


        // make ghost
        if (victimTeam != null) {
            victimTeam.removePlayer(victim);
        }
        if (event.getDestinationTeam() == null) {
            switchToSpectator(victim);
        } else {
            if (!event.getDestinationTeam().addPlayer(victim, false)) {
                switchToSpectator(victim);
            }
        }

        // spawn corpse
        PlayerCorpse corpse = CorpseManager.spawnCorpse(this, victim, victim.getLocation(), helmet, chestPlate, leggings, boots);
        if (corpse != null) {
            addDeadBody(corpse);
        }

        ItemStack item = killer.getInventory().getItemInMainHand();
        if (item != null) {
            String data = CommonManager.getINSTANCE().getItemSupport().getTag(item, CommandItemsManager.INTERACT_NBT_TAG_PLAYER_CMDS);
            if (data != null) {
                for (String line : data.split(",")) {
                    String[] cmd = line.split(" ");
                    //noinspection UnstableApiUsage
                    if (cmd.length > 1 && CommonManager.getINSTANCE().getCommonProvider().getMainCommand().hasAlias(cmd[0]) || CommonManager.getINSTANCE().getCommonProvider().getMainCommand().getName().equals(cmd[0])) {
                        ICommandNode killCmd = CommonManager.getINSTANCE().getCommonProvider().getMainCommand().getSubCommand(cmd[1]);
                        if (killCmd != null && killCmd.getName().equals("kill")) {
                            killer.setCooldown(item.getType(), getLiveSettings().getKillCooldown().getCurrentValue() * 20);
                        }
                    }
                }
            }
        }

        for (GameListener listener : getGameListeners()) {
            listener.onPlayerKill(this, killer, victim, destinationTeam, corpse);
        }

        // check game end
        getGameEndConditions().tickGameEndConditions(this);
    }

    @Override
    public void addSabotage(SabotageBase sabotageBase) {
        if (getGameState() != GameState.IN_GAME) return;
        if (loadedSabotages.contains(sabotageBase)) return;
        loadedSabotages.add(sabotageBase);
    }

    @Override
    public void removeSabotage(SabotageBase sabotageBase) {
        loadedSabotages.remove(sabotageBase);
    }

    @Override
    public List<SabotageBase> getLoadedSabotages() {
        return loadedSabotages;
    }

    @Override
    public void interruptTasks() {
        getLoadedGameTasks().forEach(task -> getPlayers().forEach(player -> {
            if (task.isDoingTask(player)) {
                task.onInterrupt(player, this);
            }
        }));
    }

    @Override
    public void interruptTasks(Player player) {
        getLoadedGameTasks().forEach(task -> {
            if (task.isDoingTask(player)) {
                task.onInterrupt(player, this);
            }
        });
    }

    @Override
    public boolean hasLoadedSabotage(String identifier) {
        return loadedSabotages.stream().anyMatch(sabotage -> sabotage.getProvider().getUniqueIdentifier().equals(identifier));
    }

    @Override
    public @Nullable SabotageBase getLoadedSabotage(String provider, String sabotageId) {
        return loadedSabotages.stream().filter(sabotage -> sabotage.getProvider().getOwner().getName().equals(provider) && sabotage.getProvider().getUniqueIdentifier().equals(sabotageId)).findFirst().orElse(null);
    }

    @Override
    public void registerGameListener(GameListener listener) {
        gameListeners.add(listener);
    }

    @Override
    public void unRegisterGameListener(GameListener listener) {
        gameListeners.remove(listener);
    }

    @Override
    public LinkedList<GameListener> getGameListeners() {
        return gameListeners;
    }

    @Override
    public boolean isTasksAllowedATM() {
        if (getGameState() != GameState.IN_GAME) return false;
        return getLoadedSabotages().stream().noneMatch(sabotage -> sabotage.isActive() && !sabotage.isAllowTasks());
    }

    @Override
    public void disableTaskIndicators() {
        if (!taskIndicatorActive) return;
        getLoadedGameTasks().forEach(GameTask::disableIndicators);
        taskIndicatorActive = false;
    }

    @Override
    public boolean tryEnableTaskIndicators() {
        if (taskIndicatorActive) return false;
        if (getLoadedSabotages().stream().noneMatch(sabotage -> sabotage.isActive() && !sabotage.isAllowTasks())) {
            getLoadedGameTasks().forEach(GameTask::enableIndicators);
            taskIndicatorActive = true;
            return true;
        }
        return false;
    }

    @Override
    public void addRoom(GameRoom room) {
        rooms.add(room);
    }

    @Override
    public void removeRoom(GameRoom room) {
        rooms.remove(room);
    }

    @Override
    public @Nullable GameRoom getPlayerRoom(Player player) {
        for (GameRoom room : rooms) {
            if (room.getRegion().isInRegion(player.getLocation())) {
                return room;
            }
        }
        return null;
    }

    @Override
    public @Nullable GameRoom getRoom(Location location) {
        for (GameRoom room : rooms) {
            if (room.getRegion().isInRegion(location)) {
                return room;
            }
        }
        return null;
    }

    @Override
    public void defeatBySabotage(@Nullable String reasonPath) {
        GameEndConditions.impostorsWin(this, reasonPath);
    }

    @Override
    public @Nullable VentHandler getVentHandler() {
        return ventHandler;
    }

    @Override
    public void setVentHandler(@Nullable VentHandler ventingHandler) {
        if (this.ventHandler != null) {
            getPlayers().forEach(player -> this.ventHandler.unVent(player, SteveSus.getInstance()));
        }
        this.ventHandler = ventingHandler;
    }

    @Override
    public GameTeamAssigner getGameTeamAssigner() {
        return teamAssigner;
    }

    @Override
    public void setGameTeamAssigner(@Nullable GameTeamAssigner gameTeamAssigner) {
        if (gameTeamAssigner == null) {
            this.teamAssigner = new GameTeamAssigner();
        } else {
            this.teamAssigner = gameTeamAssigner;
        }
    }

    @Override
    public @NotNull LiveSettings getLiveSettings() {
        return liveSettings;
    }

    @Override
    public void setLiveSettings(@NotNull LiveSettings liveSettings) {
        this.liveSettings = liveSettings;
    }

    @Override
    public @Nullable SabotageCooldown getSabotageCooldown() {
        return sabotageCooldown;
    }

    @Override
    public void setSabotageCooldown(@Nullable SabotageCooldown sabotageCooldown) {
        this.sabotageCooldown = sabotageCooldown;
    }

    @Override
    public int getActiveSabotages() {
        int result = 0;
        for (SabotageBase sabotage : loadedSabotages) {
            if (sabotage.isActive()) {
                result++;
            }
        }
        return result;
    }

    @Override
    public @Nullable CamHandler getCamHandler() {
        return camHandler;
    }

    @Override
    public void setCamHandler(@Nullable CamHandler camHandler) {
        if (getCamHandler() != null) {
            for (UUID uuid : getCamHandler().getPlayersOnCams()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    getCamHandler().stopWatching(player, this);
                }
            }
            unRegisterGameListener(SecurityListener.getInstance());
            PlayerItemHeldEvent.getHandlerList().unregister(SecurityListener.getInstance());
        }
        this.camHandler = camHandler;
    }

    public Location getNextWaitingSpawn() {
        if (waitingLocations.size() == 1) {
            return waitingLocations.get(0);
        }
        return waitingLocations.get(++currentSpawnPos >= waitingLocations.size() ? currentSpawnPos = 0 : currentSpawnPos);
    }

    public Location getNextSpectatorSpawn() {
        if (spectatingLocations.size() == 1) {
            return spectatingLocations.get(0);
        }
        return spectatingLocations.get(++currentSpectatePos >= spectatingLocations.size() ? currentSpectatePos = 0 : currentSpectatePos);
    }

    @SuppressWarnings("unused")
    public Location getNextMeetingSpawn() {
        if (meetingLocations.size() == 1) {
            return meetingLocations.get(0);
        }
        return meetingLocations.get(++currentMeetingPos >= meetingLocations.size() ? currentMeetingPos = 0 : currentMeetingPos);
    }

    @Override
    public ItemStack getDisplayItem(CommonLocale lang) {

        ItemStack item;
        switch (getGameState()) {
            case WAITING:
                item = itemWaiting;
                break;
            case STARTING:
                item = itemStarting;
                break;
            case IN_GAME:
                item = itemPlaying;
                break;
            default:
                item = itemEnding;
                break;
        }

        if (lang == null) return item;

        ItemMeta meta = item.getItemMeta();
        String displayName;
        if (lang.hasPath(ArenaHolderConfig.getNameForState(getGameState()) + "-" + getTemplateWorld())) {
            // check custom name for template
            displayName = strReplaceArenaPlaceholders(lang.getMsg(null, ArenaHolderConfig.getNameForState(getGameState()) + "-" + getTemplateWorld()), lang);
        } else {
            displayName = strReplaceArenaPlaceholders(lang.getMsg(null, ArenaHolderConfig.getNameForState(getGameState())), lang);
        }
        meta.setDisplayName(displayName);

        if (lang.hasPath(ArenaHolderConfig.getLoreForState(getGameState()) + "-" + getTemplateWorld())) {
            meta.setLore(lang.getMsgList(null, ArenaHolderConfig.getLoreForState(getGameState()) + "-" + getTemplateWorld(), new String[]{"{name}", getDisplayName(), "{template}", getTemplateWorld(), "{status}", lang.getMsg(null, getGameState().getTranslatePath()),
                    "{on}", String.valueOf(getPlayers().size()), "{max}", String.valueOf(getMaxPlayers()), "{spectating}", String.valueOf(getSpectators().size())}));
        } else {
            meta.setLore(lang.getMsgList(null, ArenaHolderConfig.getLoreForState(getGameState()), new String[]{"{name}", getDisplayName(), "{template}", getTemplateWorld(), "{status}", lang.getMsg(null, getGameState().getTranslatePath()),
                    "{on}", String.valueOf(getPlayers().size()), "{max}", String.valueOf(getMaxPlayers()), "{spectating}", String.valueOf(getSpectators().size())}));
        }
        item.setItemMeta(meta);
        return item;
    }

    private String strReplaceArenaPlaceholders(String in, CommonLocale lang) {
        return in.replace("{name}", getDisplayName()).replace("{template}", getTemplateWorld()).replace("{status}", lang.getMsg(null, getGameState().getTranslatePath()))
                .replace("{on}", String.valueOf(getPlayers().size())).replace("{max}", String.valueOf(getMaxPlayers()))
                .replace("{spectating}", String.valueOf(getSpectators().size())).replace("{game_tag}", getTag()).replace("{game_id}", String.valueOf(getGameId()));
    }

    private void initTasks() {
        config.getProperty(ArenaConfig.TASKS).forEach(taskString -> {
            String[] taskData = taskString.split(";");
            if (taskData.length == 4) {
                String taskName = taskData[0];
                String providerName = taskData[1];
                String taskIdentifier = taskData[2];
                JsonObject taskConfiguration = new JsonParser().parse(taskData[3]).getAsJsonObject();
                TaskProvider taskProvider = ArenaManager.getINSTANCE().getTask(providerName, taskIdentifier);
                if (taskProvider == null) {
                    SteveSus.getInstance().getLogger().warning("Could not load game task " + taskName + " on " + getTemplateWorld() + " on id:" + getTag() + " because task provider (" + providerName + ") was not found!");
                } else {
                    GameTask result = taskProvider.onGameInit(this, taskConfiguration, taskName);
                    if (result == null || result.getHandler() == null) {
                        SteveSus.debug("Could not initialize game task " + taskName + " on " + getTemplateWorld() + " id: " + getTag());
                    } else {
                        this.gameTasks.add(result);
                        SteveSus.debug("Initialized game task " + taskName + " on " + getTemplateWorld() + " id: " + getTag());
                    }
                }
            } else {
                if (taskData.length > 0) {
                    SteveSus.getInstance().getLogger().warning("Could not load game task " + taskData[0] + " on " + getTemplateWorld() + " id: " + getTag() + ". Bad data!");
                }
            }
        });
    }

    private void initSabotages() {
        config.getProperty(ArenaConfig.SABOTAGES).forEach(sabotageString -> {
            String[] data = sabotageString.split(";");
            if (data.length > 2) {
                SabotageProvider provider = ArenaManager.getINSTANCE().getSabotageProviderByName(data[0], data[1]);
                if (provider == null) {
                    SteveSus.getInstance().getLogger().warning("Could not load game sabotage " + data[0] + ":" + data[1] + " on " + getTemplateWorld() + "(" + getTag() + "), it was not found!");
                } else {
                    SabotageBase sabotage = provider.onArenaInit(this, new JsonParser().parse(data[2]).getAsJsonObject());
                    if (sabotage == null) {
                        SteveSus.debug("Could not initialize game sabotage " + data[0] + ":" + data[1] + " on " + getTemplateWorld() + " id: " + getTag());

                    } else {
                        loadedSabotages.add(sabotage);
                        SteveSus.debug("Initialized game sabotage " + data[0] + ":" + data[1] + " on " + getTemplateWorld() + " id: " + getTag());
                    }
                }
            }
        });
    }

    private void initRooms() {
        for (String room : config.getProperty(ArenaConfig.ROOMS)) {
            String[] data = room.split(";");
            if (data.length > 2) {
                OrphanLocationProperty exporter = new OrphanLocationProperty();
                Location pos1 = exporter.convert(data[1], null);
                Location pos2 = exporter.convert(data[2], null);
                if (pos1 == null || pos2 == null) {
                    SteveSus.getInstance().getLogger().warning("Could not load game room: " + data[0] + "(bad data).");
                    continue;
                }
                CuboidRegion region = new CuboidRegion(pos1, pos2, true);
                addRoom(new GameRoom(region, data[0]));
                SteveSus.getInstance().getLogger().info("Loaded game room: " + data[0] + ".");
            }
        }
    }

    public void initVents() {
        ventHandler = new VentHandler(this, new LinkedList<>());
        OrphanLocationProperty locImporter = new OrphanLocationProperty();

        for (String ventString : config.getProperty(ArenaConfig.VENTS)) {
            String[] ventData = ventString.split(";");
            if (ventData.length > 2) {
                String ventName = ventData[0];
                Location ventLoc = locImporter.convert(ventData[2], null);
                if (ventLoc == null) {
                    SteveSus.getInstance().getLogger().warning("Could not load vent " + ventName + " on " + getTemplateWorld() + "(" + getGameId() + "). Bad location!");
                    continue;
                }
                ventLoc.setWorld(getWorld());
                ItemStack displayItem = null;
                if (ventData.length > 3) {
                    String[] displayData = ventData[3].split(",");
                    String displayMaterial = "BEDROCK";
                    byte data = 0;
                    if (displayData.length > 0) {
                        displayMaterial = displayData[0];
                    }
                    if (displayData.length > 1) {
                        data = Byte.parseByte(displayData[1]);
                    }
                    displayItem = CommonManager.getINSTANCE().getItemSupport().createItem(displayMaterial, 1, data);
                }
                Vent vent = new Vent(ventName, ventLoc, displayItem == null ? new ItemStack(Material.BEDROCK) : displayItem);
                for (String ventConn : ventData[1].split(",")) {
                    Vent conn = ventHandler.getVent(ventConn);
                    if (conn != null) {
                        vent.addConnection(conn);
                        conn.addConnection(vent);
                    }
                }
                ventHandler.addVent(vent);
            }
        }

    }

    private void createTaskMeterBar() {
        if (taskMeterBar == null) {
            taskMeterBar = Bukkit.createBossBar(LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, Message.GAME_TASK_METER_NAME), BarColor.GREEN, BarStyle.SOLID);
        }
        taskMeterBar.setProgress(0);
        getPlayers().forEach(player -> taskMeterBar.addPlayer(player));
        getSpectators().forEach(spectator -> taskMeterBar.addPlayer(spectator));
    }

    private void removeTaskMeter(Player player) {
        if (taskMeterBar != null) {
            taskMeterBar.removePlayer(player);
        }
    }

    private void sendTaskMeter(Player player) {
        if (taskMeterBar != null) {
            taskMeterBar.addPlayer(player);
        }
    }
}
