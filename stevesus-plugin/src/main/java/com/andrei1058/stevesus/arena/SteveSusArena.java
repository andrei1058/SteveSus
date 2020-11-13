package com.andrei1058.stevesus.arena;

import ch.jalu.configme.SettingsManager;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.api.arena.Arena;
import com.andrei1058.stevesus.api.arena.ArenaTime;
import com.andrei1058.stevesus.api.arena.GameEndConditions;
import com.andrei1058.stevesus.api.arena.meeting.ExclusionVoting;
import com.andrei1058.stevesus.api.arena.meeting.MeetingButton;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.task.*;
import com.andrei1058.stevesus.api.arena.team.GameTeamAssigner;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.event.*;
import com.andrei1058.stevesus.api.locale.ChatUtil;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.api.server.GameSound;
import com.andrei1058.stevesus.api.server.ServerType;
import com.andrei1058.stevesus.arena.meeting.MeetingSound;
import com.andrei1058.stevesus.arena.meeting.VoteGUIManager;
import com.andrei1058.stevesus.arena.runnable.ArenaTaskPlaying;
import com.andrei1058.stevesus.arena.runnable.ArenaTaskRestarting;
import com.andrei1058.stevesus.arena.runnable.ArenaTaskStarting;
import com.andrei1058.stevesus.arena.team.CrewTeam;
import com.andrei1058.stevesus.arena.team.GhostCrewTeam;
import com.andrei1058.stevesus.arena.team.GhostImpostorTeam;
import com.andrei1058.stevesus.arena.team.ImpostorTeam;
import com.andrei1058.stevesus.commanditem.InventoryUtil;
import com.andrei1058.stevesus.commanditem.JoinItemsManager;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.api.selector.ArenaHolderConfig;
import com.andrei1058.stevesus.common.gui.ItemUtil;
import com.andrei1058.stevesus.common.party.PartyManager;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.language.LanguageManager;
import com.andrei1058.stevesus.prevention.PreventionManager;
import com.andrei1058.stevesus.server.ServerCommonProvider;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.multiarena.InventoryBackup;
import com.andrei1058.stevesus.sidebar.GameSidebarManager;
import com.andrei1058.stevesus.sidebar.SidebarType;
import com.andrei1058.stevesus.worldmanager.WorldManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
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
    private boolean visualTasksEnabled;
    private int commonTasks = 1;
    private int shortTasks = 2;
    private int longTasks = 1;
    private TaskMeterUpdatePolicy taskMeterUpdatePolicy = TaskMeterUpdatePolicy.ALWAYS;
    private MeetingStage meetingStage = MeetingStage.NO_MEETING;
    private BossBar taskMeterBar;

    private final List<UUID> freeze = new ArrayList<>(); // list of players that cannot move
    private final HashMap<UUID, Integer> meetingsLeft = new HashMap<>();
    private MeetingButton meetingButton;
    private int meetingsPerPlayer;
    private int talkingDuration;
    private int votingDuration;
    private ExclusionVoting currentExclusionVoting;
    private boolean anonymousVotes = false;
    private boolean emergency = false;
    private GameEndConditions gameEndConditions;

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
        this.visualTasksEnabled = config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_VISUAL_ENABLED);
        setTaskMeterUpdatePolicy(TaskMeterUpdatePolicy.ALWAYS);
        this.meetingsPerPlayer = config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_PER_PLAYER);
        this.talkingDuration = config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_TALK_TIME);
        this.votingDuration = config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_VOTE_TIME);
        this.gameEndConditions = ArenaManager.getINSTANCE().getGameEndConditions();
    }

    private void restoreCountDown() {
        countdown = config.getProperty(ArenaConfig.GAME_COUNTDOWN_INITIAL).orElse(ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.GAME_COUNTDOWN_INITIAL));
    }

    @Override
    public void init(World world) {
        this.world = world;
        this.world.setKeepSpawnInMemory(true);
        this.world.setAutoSave(false);

        waitingLocations.forEach(location -> location.setWorld(this.world));
        spectatingLocations.forEach(location -> location.setWorld(this.world));
        meetingLocations.forEach(location -> location.setWorld(this.world));

        teams.add(new CrewTeam(this));
        teams.add(new ImpostorTeam(this, 1));
        teams.add(new GhostCrewTeam(this));
        teams.add(new GhostImpostorTeam(this));

        if (getTime() != null) {
            world.setTime(getTime().getStartTick());
        }

        initTasks();
        commonTasks = Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_COMMON), (int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.COMMON).count());
        shortTasks = Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_SHORT), (int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.SHORT).count());
        longTasks = Math.min(config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_TASKS_LONG), (int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.LONG).count());

        spawnMeetingButton(world);

        switchState(GameState.WAITING);
    }

    private void spawnMeetingButton(World world) {
        Location location = config.getProperty(ArenaConfig.MEETING_BUTTON_LOC).orElse(null);
        if (location != null) {
            location.setWorld(world);
            meetingButton = new MeetingButton(SteveSus.getInstance(), location, this, config.getProperty(ArenaConfig.DEFAULT_GAME_OPTION_MEETING_COOL_DOWN));
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

        //todo cancel tasks
    }

    @Override
    public void restart() {
        if (gameTask != -1) {
            Bukkit.getScheduler().cancelTask(gameTask);
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
            JoinItemsManager.sendCommandItems(player, getGameState() == GameState.STARTING ? JoinItemsManager.CATEGORY_STARTING : JoinItemsManager.CATEGORY_WAITING);

            // play songs before adding him to the players list so you don't have to filter it. it would be redundant.
            GameSound.JOIN_SOUND_CURRENT.playToPlayers(getPlayers());

            players.add(player);
            ArenaManager.getINSTANCE().setArenaByPlayer(player, this);

            for (Player on : players) {
                on.sendMessage(LanguageManager.getINSTANCE().getMsg(on, Message.ARENA_JOIN_ANNOUNCE).replace("{player}", player.getDisplayName())
                        .replace("{on}", String.valueOf(getPlayers().size())).replace("{max}", String.valueOf(getMaxPlayers())));
                //todo add pop sound
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
                    if (isPlayer(onServer)) {
                        onServer.showPlayer(SteveSus.getInstance(), player);
                        player.showPlayer(SteveSus.getInstance(), onServer);
                    } else {
                        onServer.hidePlayer(SteveSus.getInstance(), player);
                        player.hidePlayer(SteveSus.getInstance(), onServer);

                    }

                }
            }).execute();

            // trigger game task event
            // so they can send their custom data etc.
            gameTasks.forEach(gameTask1 -> gameTask1.onPlayerJoin(this, player, false));

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

        if (target != null) {
            target.setWorld(getWorld());
        }
        player.teleport(target == null ? getNextSpectatorSpawn() : target, PlayerTeleportEvent.TeleportCause.PLUGIN);

        InventoryUtil.wipePlayer(player);

        // send items
        JoinItemsManager.sendCommandItems(player, JoinItemsManager.CATEGORY_SPECTATING);
        // give scoreboard
        GameSidebarManager.getInstance().setSidebar(player, SidebarType.SPECTATOR, this, ServerManager.getINSTANCE().getServerType() != ServerType.MULTI_ARENA);

        spectators.add(player);
        ArenaManager.getINSTANCE().setArenaByPlayer(player, this);
        player.setGameMode(GameMode.ADVENTURE);

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

        // trigger game task event
        // so they can send their custom data etc.
        gameTasks.forEach(gameTask1 -> gameTask1.onPlayerJoin(this, player, true));
        sendTaskMeter(player);
        SteveSus.debug("Player " + player.getName() + " was added as spectator to game " + getGameId() + "(" + getTemplateWorld() + ").");
        return true;
    }

    @Override
    public boolean switchToSpectator(Player player) {
        if (!isPlayer(player)) return false;
        players.remove(player);
        spectators.add(player);

        // call event
        PlayerToSpectatorEvent playerToSpectatorEvent = new PlayerToSpectatorEvent(this, player);
        Bukkit.getPluginManager().callEvent(playerToSpectatorEvent);

        player.closeInventory();
        // tp to spectator spawn
        player.teleport(getNextSpectatorSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        // clear inv
        InventoryUtil.wipePlayer(player);
        // send items
        JoinItemsManager.sendCommandItems(player, JoinItemsManager.CATEGORY_SPECTATING);
        // change gm
        player.setGameMode(GameMode.ADVENTURE);
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

        //todo
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

        // if in game status, will check if it is the case to end this game
        getGameEndConditions().tickGameEndConditions(this);

        for (Player inArena : getPlayers()) {
            inArena.sendMessage(LanguageManager.getINSTANCE().getMsg(inArena, Message.LEAVE_ANNOUNCE).replace("{player}", player.getDisplayName())
                    .replace("{on}", String.valueOf(getPlayers().size())).replace("{max}", String.valueOf(getMaxPlayers())));
        }

        for (Player inArena : getSpectators()) {
            inArena.sendMessage(LanguageManager.getINSTANCE().getMsg(inArena, Message.LEAVE_ANNOUNCE).replace("{player}", player.getDisplayName())
                    .replace("{on}", String.valueOf(getPlayers().size())).replace("{max}", String.valueOf(getMaxPlayers())));
        }
        setCantMove(player, false);
        meetingsLeft.remove(player.getUniqueId());
        removeTaskMeter(player);
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
            if (getTaskMeterUpdatePolicy() != TaskMeterUpdatePolicy.NEVER) {
                createTaskMeterBar();
            }
            gameStart = Instant.now();
            getPlayers().forEach(p -> {
                p.getInventory().clear();
                p.teleport(getNextMeetingSpawn(), PlayerTeleportEvent.TeleportCause.PLUGIN);
                meetingsLeft.putIfAbsent(p.getUniqueId(), getMeetingsPerPlayer());
            });
            gameTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), new ArenaTaskPlaying(this), 0L, 20L).getTaskId();
            // assign teams
            GameTeamAssigner teamAssigner = new GameTeamAssigner(this);
            teamAssigner.assignTeams();
            // assign tasks
            GameTaskAssigner gameTaskAssigner = new GameTaskAssigner(this);
            teams.forEach(gameTaskAssigner::assignTasks);
            getPlayers().forEach(player -> GameSidebarManager.getInstance().setSidebar(player, SidebarType.IN_GAME, this, false));
            if (getMeetingButton() != null) {
                getMeetingButton().refreshLines(this);
            }
        } else if (gameState == GameState.ENDING) {
            getPlayers().forEach(player -> GameSidebarManager.getInstance().setSidebar(player, SidebarType.ENDING, this, false));
            getSpectators().forEach(player -> GameSidebarManager.getInstance().setSidebar(player, SidebarType.ENDING, this, false));
            gameTask = Bukkit.getScheduler().runTaskTimer(SteveSus.getInstance(), new ArenaTaskRestarting(this), 0L, 20L).getTaskId();
        }

        gameTasks.forEach(task -> task.onGameStateChange(oldState, gameState, this));
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
        if (!isSpectator(spectator)) return;
        if (isFirstPersonSpectate(spectator)) return;

        spectator.setGameMode(GameMode.ADVENTURE);
        spectator.setAllowFlight(true);
        spectator.setFlying(true);

        Player target;

        SpectatorFirstPersonEvent spectatorFirstPersonEvent = new SpectatorFirstPersonEvent(this, spectator, target = (Player) spectator.getSpectatorTarget(), SpectatorFirstPersonEvent.SpectateAction.STOP);
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
    public boolean isVisualTasksEnabled() {
        return visualTasksEnabled;
    }

    @Override
    public void setVisualTasksEnabled(boolean toggle) {
        this.visualTasksEnabled = toggle;
    }

    @Override
    public int getCommonTasks() {
        return commonTasks;
    }

    @Override
    public void setCommonTasks(int commonTasks) {
        if (commonTasks < 0) return;
        if (!(getGameState() == GameState.WAITING || getGameState() == GameState.STARTING)) return;
        if ((int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.COMMON).count() < commonTasks) {
            return;
        }
        this.commonTasks = commonTasks;
    }

    @Override
    public int getShortTasks() {
        return shortTasks;
    }

    @Override
    public void setShortTasks(int shortTasks) {
        if (shortTasks < 0) return;
        if (!(getGameState() == GameState.WAITING || getGameState() == GameState.STARTING)) return;
        if ((int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.SHORT).count() < shortTasks) {
            return;
        }
        this.shortTasks = shortTasks;
    }

    @Override
    public int getLongTasks() {
        return longTasks;
    }

    @Override
    public void setLongTasks(int longTasks) {
        if (longTasks < 0) return;
        if (!(getGameState() == GameState.WAITING || getGameState() == GameState.STARTING)) return;
        if ((int) gameTasks.stream().filter(task -> task.getHandler().getTaskType() == TaskType.LONG).count() < longTasks) {
            return;
        }
        this.longTasks = longTasks;
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
        if (taskMeterUpdatePolicy == TaskMeterUpdatePolicy.NEVER) return;
        if (taskMeterUpdatePolicy == TaskMeterUpdatePolicy.MEETINGS && getMeetingStage() == MeetingStage.NO_MEETING) {
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
    public TaskMeterUpdatePolicy getTaskMeterUpdatePolicy() {
        return taskMeterUpdatePolicy;
    }

    @Override
    public void setTaskMeterUpdatePolicy(TaskMeterUpdatePolicy taskMeterPolicy) {
        if (this.taskMeterUpdatePolicy == TaskMeterUpdatePolicy.NEVER && taskMeterPolicy != TaskMeterUpdatePolicy.NEVER) {
            if (getGameState() == GameState.IN_GAME) {
                createTaskMeterBar();
            }
        }
        if (this.taskMeterUpdatePolicy == TaskMeterUpdatePolicy.NEVER) {
            if (this.taskMeterBar != null) {
                this.taskMeterBar.removeAll();
                this.taskMeterBar = null;
            }
        }
        this.taskMeterUpdatePolicy = taskMeterPolicy;
    }

    @Override
    public MeetingStage getMeetingStage() {
        return meetingStage;
    }

    @Override
    public void setMeetingStage(MeetingStage meetingStage) {
        if (meetingStage == this.meetingStage) return;
        this.meetingStage = meetingStage;
        if (meetingStage == MeetingStage.NO_MEETING) {
            if (getMeetingButton() != null) {
                getMeetingButton().refreshLines(this);
                getMeetingButton().setLastUsage(System.currentTimeMillis());
            }
            getPlayers().forEach(player -> {
                setCantMove(player, false);
                player.closeInventory();
                player.getInventory().clear();
            });
            getGameEndConditions().tickGameEndConditions(this);
        } else if (meetingStage == MeetingStage.TALKING) {
            setCountdown(getMeetingTalkDuration());
            getPlayers().forEach(player -> setCantMove(player, true));
        } else if (meetingStage == MeetingStage.VOTING) {
            setCountdown(getMeetingVotingDuration());
            getPlayers().forEach(player -> {
                setCantMove(player, true);
                VoteGUIManager.openToPlayer(player, this);
                JoinItemsManager.sendCommandItems(player, JoinItemsManager.CATEGORY_VOTING);
            });
        } else if (meetingStage == MeetingStage.EXCLUSION_SCREEN) {
            setCountdown(5);
            getPlayers().forEach(player -> {
                setCantMove(player, false);
                player.closeInventory();
                player.getInventory().clear();
            });
            if (currentExclusionVoting != null) {
                currentExclusionVoting.performExclusion(this, null);
                setCurrentVoting(null);
            }
        }
    }

    @Override
    public boolean startMeeting(Player requester, @Nullable Player deadBody) {
        if (getGameState() != GameState.IN_GAME) return false;
        if (getMeetingStage() != MeetingStage.NO_MEETING) return false;
        Team playerTeam = getPlayerTeam(requester);
        if (playerTeam == null) return false;
        if (deadBody == null) {
            if (!playerTeam.canUseMeetingButton()) {
                return false;
            }
        } else {
            if (!playerTeam.canReportBody()) {
                return false;
            }
        }
        setCurrentVoting(new ExclusionVoting(this));
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

            MeetingSound.playMusic(this, 0);

            // todo replace room placeholder in messages
            getPlayers().forEach(player -> {
                Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                lang.getMsgList(player, Message.MEETING_START_CHAT_MSG_BODY.toString(), new String[]{"{reporter}", requester.getDisplayName(), "{dead}", deadBody.getDisplayName()}).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                player.sendTitle(lang.getMsg(player, Message.EMERGENCY_MEETING_DEAD_TITLE).replace("{reporter}", requester.getDisplayName().replace("{dead}", deadBody.getDisplayName())), lang.getMsg(player, Message.EMERGENCY_MEETING_DEAD_SUBTITLE).replace("{reporter}", requester.getDisplayName().replace("{dead}", deadBody.getDisplayName())), 0, 80, 0);
            });
            getSpectators().forEach(player -> {
                Locale lang = LanguageManager.getINSTANCE().getLocale(player);
                lang.getMsgList(player, Message.MEETING_START_CHAT_MSG_BODY.toString(), new String[]{"{reporter}", requester.getDisplayName(), "{dead}", deadBody.getDisplayName()}).forEach(string -> player.sendMessage(ChatUtil.centerMessage(string)));
                player.sendTitle(lang.getMsg(player, Message.EMERGENCY_MEETING_DEAD_TITLE).replace("{reporter}", requester.getDisplayName().replace("{dead}", deadBody.getDisplayName())), lang.getMsg(player, Message.EMERGENCY_MEETING_DEAD_SUBTITLE).replace("{reporter}", requester.getDisplayName().replace("{dead}", deadBody.getDisplayName())), 0, 80, 0);
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
    public int getMeetingsPerPlayer() {
        return meetingsPerPlayer;
    }

    @Override
    public void setMeetingsPerPlayer(int value) {
        if (getGameState() == GameState.IN_GAME) return;
        this.meetingsPerPlayer = value;
    }

    @Override
    public void setMeetingsLeft(Player player, int amount) {
        if (!isPlayer(player)) return;
        meetingsLeft.remove(player.getUniqueId());
        meetingsLeft.put(player.getUniqueId(), amount);
    }

    @Override
    public void setMeetingTalkDuration(int value) {
        this.talkingDuration = value;
    }

    @Override
    public int getMeetingTalkDuration() {
        return talkingDuration;
    }

    @Override
    public void setMeetingVotingDuration(int value) {
        this.votingDuration = value;
    }

    @Override
    public int getMeetingVotingDuration() {
        return votingDuration;
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
    public boolean isAnonymousVotes() {
        return anonymousVotes;
    }

    @Override
    public void setAnonymousVotes(boolean toggle) {
        this.anonymousVotes = toggle;
    }

    @Override
    public boolean isEmergency() {
        return emergency;
    }

    @Override
    public void setEmergency(boolean toggle) {
        if (toggle == this.emergency) return;
        this.emergency = toggle;
        if (toggle) {
            getLoadedGameTasks().forEach(loadedTask -> loadedTask.onEmergencyStart(this));
        } else {
            getLoadedGameTasks().forEach(loadedTask -> loadedTask.onEmergencyEnd(this));
        }
    }

    @Override
    public void setGameEndConditions(@NotNull GameEndConditions gameEndConditions) {
        this.gameEndConditions = gameEndConditions;
    }

    @Override
    public @NotNull GameEndConditions getGameEndConditions() {
        return gameEndConditions;
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
                    if (result == null) {
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
