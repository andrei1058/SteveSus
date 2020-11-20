package com.andrei1058.stevesus.api.arena;

import com.andrei1058.stevesus.api.arena.meeting.ExclusionVoting;
import com.andrei1058.stevesus.api.arena.meeting.MeetingButton;
import com.andrei1058.stevesus.api.arena.meeting.MeetingStage;
import com.andrei1058.stevesus.api.arena.room.GameRoom;
import com.andrei1058.stevesus.api.arena.sabotage.SabotageBase;
import com.andrei1058.stevesus.api.arena.task.GameTask;
import com.andrei1058.stevesus.api.arena.task.TaskMeterUpdatePolicy;
import com.andrei1058.stevesus.api.arena.team.PlayerColorAssigner;
import com.andrei1058.stevesus.api.arena.team.Team;
import com.andrei1058.stevesus.api.arena.vent.VentHandler;
import com.andrei1058.stevesus.api.locale.Locale;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.arena.DisplayableArena;
import com.andrei1058.stevesus.common.api.arena.GameState;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public interface Arena extends DisplayableArena {

    /**
     * Used to assign a world to the arena.
     * Usually handled by Bukkit's world load event.
     * This will declare the arena as ready.
     *
     * @param world assigned world.
     */
    void init(World world);

    /**
     * Triggered when server is shutting down, requested by command or other.
     */
    void disable();

    /**
     * Triggered on game end.
     */
    void restart();

    /**
     * Get game id.
     *
     * @return game id.
     */
    int getGameId();

    /**
     * Get arena world.
     *
     * @return world.
     */
    World getWorld();

    /**
     * Get the name of the original world that was cloned for this match.
     *
     * @return the name of the original world that was cloned for this match.
     */
    String getTemplateWorld();

    /**
     * Get arena game state.
     *
     * @return arena status.
     */
    GameState getGameState();

    /**
     * Get game state in player's language.
     *
     * @param player player.
     */
    String getDisplayState(@NotNull Player player);

    /**
     * Get game state in given language.
     *
     * @param language target language.
     */
    String getDisplayState(@Nullable Locale language);

    /**
     * Check if the arena is full.
     * Usually used at waiting and starting {@link GameState}.
     */
    boolean isFull();

    /**
     * Get time for game began.
     */
    Instant getStartTime();

    /**
     * Get players in this game.
     *
     * @return unmodifiable list of players in game.
     */
    List<Player> getPlayers();

    /**
     * Get spectators in this arena.
     *
     * @return unmodifiable list of spectators.
     */
    List<Player> getSpectators();

    /**
     * Check if this arena allows spectating.
     *
     * @return true if allows spectating.
     */
    String getSpectatePermission();

    /**
     * Allow players to join as spectators.
     *
     * @param spectatePermission true to allow spectating.
     */
    void setSpectatePermission(String spectatePermission);

    /**
     * Add a player to the game.
     * This must be used only on {@link GameState#WAITING} and {@link GameState#STARTING}.
     * Will return false if player is already in a game.
     *
     * @param player      player to be added.
     * @param ignoreParty if it doesn't matter if he is the party owner.
     * @return true if added successfully.
     */
    boolean addPlayer(Player player, boolean ignoreParty);

    default boolean joinPlayer(Player player, boolean ignoreParty) {
        return addPlayer(player, ignoreParty);
    }

    /**
     * Join the game as Spectator.
     * If the player have played on this game and you want to add him
     * to spectators use {@link #switchToSpectator(Player)}.
     *
     * @param player player to be added.
     * @param target start spectating at this location. Null for default.
     * @return if added to spectators successfully.
     */
    boolean addSpectator(Player player, @Nullable Location target);

    default boolean joinSpectator(Player player, @Nullable String target) {
        Location targetLocation = null;
        if (target != null) {
            UUID uuid = UUID.fromString(target);
            Player targetPlayer = Bukkit.getPlayer(uuid);
            if (targetPlayer != null) {
                targetLocation = targetPlayer.getLocation();
            }
        }
        return addSpectator(player, targetLocation);
    }

    /**
     * Move a player to spectators.
     * Used when a player is eliminated.
     * If you want to add a regular spectator use {@link #addSpectator(Player, Location).}
     *
     * @param player player to be removed from players list.
     * @return true if switched successfully.
     */
    boolean switchToSpectator(Player player);

    /**
     * Get arena countdown.
     * Used at starting/ ending.
     */
    int getCountdown();

    /**
     * Check if a player is actually playing and not spectating.
     *
     * @param player target player.
     * @return true if is playing.
     */
    boolean isPlayer(Player player);

    /**
     * Check if a player is spectator.
     *
     * @param player target player.
     * @return true if a player is spectating.
     */
    boolean isSpectator(Player player);

    /**
     * Handle player remove logic.
     *
     * @param player actual player. Not spectator or else.
     * @param onQuit if called on server quit.
     */
    void removePlayer(Player player, boolean onQuit);

    /**
     * Handle spectator remove logic.
     *
     * @param player actual spectator. Not player or else.
     * @param onQuit if called on server quit.
     */
    void removeSpectator(Player player, boolean onQuit);

    /**
     * Switch arena state.
     * Used to change from waiting to starting etc.
     * This contains game tasks logic as well.
     *
     * @param gameState new game state.
     * @return true if switched successfully.
     */
    boolean switchState(GameState gameState);

    /**
     * Get arena display name.
     *
     * @return arena display name.
     */
    String getDisplayName();

    /**
     * Set arena display name.
     *
     * @param displayName new display name.
     */
    void setDisplayName(String displayName);

    /**
     * Get max mount of players allowed to join.
     *
     * @return max players.
     */
    int getMaxPlayers();

    /**
     * Set max players allowed to join the game.
     *
     * @param maxPlayers max players.
     */
    void setMaxPlayers(int maxPlayers);

    /**
     * Get required players to start countdown.
     *
     * @return required players amount to start countdown.
     */
    int getMinPlayers();

    /**
     * Set min players required to start countdown.
     *
     * @param minPlayers min players.
     */
    void setMinPlayers(int minPlayers);

    /**
     * Change countdown seconds.
     *
     * @param seconds new seconds.
     */
    void setCountdown(int seconds);

    /**
     * Create a JSON object with arena info to be sent to remote lobbies.
     * Used in BUNGEE mode.
     */
    default JsonObject toJSON() {
        JsonObject json = new JsonObject();
        json.addProperty("gameId", getGameId());
        json.addProperty("displayName", getDisplayName());
        json.addProperty("players", getPlayers().size());
        json.addProperty("spectators", getSpectators().size());
        json.addProperty("status", getGameState().getStateCode());
        json.addProperty("maxPlayers", getMaxPlayers());
        json.addProperty("minPlayers", getMinPlayers());
        json.addProperty("spectate", getSpectatePermission());
        json.addProperty("template", getTemplateWorld());
        json.addProperty("vips", getPlayers().stream().filter(p -> CommonManager.getINSTANCE().hasVipJoin(p)).count());

        ItemStack itemStack = getDisplayItem(null);
        if (itemStack != null) {
            JsonObject statusItem = new JsonObject();
            statusItem.addProperty("material", itemStack.getType().toString());
            //noinspection deprecation
            statusItem.addProperty("data", itemStack.getData().getData());
            statusItem.addProperty("enchanted", itemStack.getEnchantments().size() != 0);
            json.add("displayItem", statusItem);
        }
        return json;
    }

    @Override
    default boolean isLocal() {
        return true;
    }

    /**
     * Check if current game can be force-started.
     * If minimum requirements are reached.
     * This is only used by force-start command.
     *
     * @return true if a vip can force-start this game.
     */
    boolean canForceStart();

    /**
     * Make a spectator spectate target in first person.
     *
     * @param spectator player spectator.
     * @param target    target player.
     */
    void startFirstPersonSpectate(Player spectator, Player target);

    /**
     * Remove a spectator from first person spectating.
     *
     * @param spectator spectator to be removed from first person.
     */
    void stopFirstPersonSpectate(Player spectator);

    /**
     * Check if the given user is spectating in first person.
     *
     * @param spectator spectator to be checked.
     */
    boolean isFirstPersonSpectate(Player spectator);

    /**
     * Get gameplay time.
     *
     * @return null if time is not handled internally. (vanilla day cycle).
     */
    @Nullable
    ArenaTime getTime();

    /**
     * Check if visual tasks are enabled.
     */
    boolean isVisualTasksEnabled();

    /**
     * Toggle visual tasks.
     */
    void setVisualTasksEnabled(boolean toggle);

    /**
     * Get number of common tasks.
     */
    int getCommonTasks();

    /**
     * Set number of common tasks.
     * Changes allowed during waiting and starting phase.
     */
    void setCommonTasks(int commonTasks);

    /**
     * Get amount of short tasks.
     */
    int getShortTasks();

    /**
     * Set number of tasks.
     * Changes allowed during waiting and starting phase.
     */
    void setShortTasks(int shortTasks);

    /**
     * Get amount of long tasks.
     */
    int getLongTasks();

    /**
     * Set number of tasks.
     * Changes allowed during waiting and starting phase.
     */
    void setLongTasks(int longTasks);

    /**
     * Get loaded game tasks.
     */
    LinkedList<GameTask> getLoadedGameTasks();

    /**
     * Get a player team.
     */
    Team getPlayerTeam(Player player);

    /**
     * Get team by technical name.
     */
    Team getTeamByName(String name);

    /**
     * Get arena teams.
     */
    List<Team> getGameTeams();

    /**
     * Freeze a player.
     * This will prevent players from moving to another block.
     * Can be used for med bay task etc.
     */
    void setCantMove(Player player, boolean toggle);

    /**
     * Check if a player cannot move.
     * This will prevent players from moving to another block.
     */
    boolean isCantMove(Player player);

    /**
     * Refresh task boss bar.
     * Update policy is handled internally so you don't need to do checks.
     */
    void refreshTaskMeter();

    /**
     * Get arena task meter policy.
     */
    TaskMeterUpdatePolicy getTaskMeterUpdatePolicy();

    /**
     * Change task meter update policy.
     */
    void setTaskMeterUpdatePolicy(TaskMeterUpdatePolicy taskMeterPolicy);

    /**
     * Get current meeting stage.
     */
    MeetingStage getMeetingStage();

    /**
     * Change meeting stage.
     */
    void setMeetingStage(MeetingStage meetingStage);

    /**
     * Start meeting.
     *
     * @param requester meeting requester.
     * @param deadBody  found body if "dead body report", otherwise emergency meeting.
     * @return false if cannot start meeting. (can return false only if no body provided).
     */
    boolean startMeeting(Player requester, @Nullable Player deadBody);

    /**
     * Get player meetings left.
     */
    int getMeetingsLeft(Player player);

    /**
     * Get meeting button.
     */
    @Nullable
    MeetingButton getMeetingButton();

    /**
     * Get how many meetings can a player start.
     */
    int getMeetingsPerPlayer();

    /**
     * Set meetings per player.
     * Can do that only if the game didn't start already.
     * If you want to give a player more meetings use {@link #setMeetingsLeft(Player, int)}.
     */
    void setMeetingsPerPlayer(int value);

    /**
     * Set how many meetings can a player start.
     */
    void setMeetingsLeft(Player player, int amount);

    /**
     * How long meeting talk takes.
     */
    void setMeetingTalkDuration(int value);

    /**
     * How long meeting talk takes.
     */
    int getMeetingTalkDuration();

    /**
     * How long voting takes.
     */
    void setMeetingVotingDuration(int value);

    /**
     * How long voting takes.
     */
    int getMeetingVotingDuration();

    /**
     * Get current voting data.
     */
    @Nullable
    ExclusionVoting getCurrentVoting();

    /**
     * Set current vote keeper.
     * Usually set to null when meeting ended.
     */
    void setCurrentVoting(@Nullable ExclusionVoting exclusionVoting);

    /**
     * Check if votes are confidential.
     */
    boolean isAnonymousVotes();

    /**
     * Set if votes are confidential.
     */
    void setAnonymousVotes(boolean toggle);

    /**
     * @return true when there is an emergency and players shouldn't be allowed to do tasks.
     */
    boolean isEmergency();

    /**
     * When set to true will disable things like tasks effects and player ability to do tasks.
     */
    void setEmergency(boolean toggle);

    /**
     * Set arena game end checker.
     */
    void setGameEndConditions(@NotNull GameEndConditions gameEndConditions);

    /**
     * Get game end checker.
     */
    @NotNull
    GameEndConditions getGameEndConditions();

    /**
     * Get player color assigner.
     */
    @Nullable
    PlayerColorAssigner<?> getPlayerColorAssigner();

    /**
     * Change player color assigner.
     */
    void setPlayerColorAssigner(@Nullable PlayerColorAssigner<?> playerColorAssigner);

    /**
     * Ignore color limit?
     * If players amount is greater than color limit some players will have the same color.
     */
    void setIgnoreColorLimit(boolean toggle);

    /**
     * Ignore color limit?
     * If players amount is greater than color limit some players will have the same color.
     */
    boolean isIgnoreColorLimit();

    /**
     * Get list dead bodies.
     * This list is cleared when a meeting starts.
     */
    List<PlayerCorpse> getDeadBodies();

    /**
     * Add dead body to bodies list.
     */
    void addDeadBody(PlayerCorpse playerCorpse);

    /**
     * Remove a dead body.
     */
    void removeDeadBody(PlayerCorpse playerCorpse);

    /**
     * Get a player body.
     */
    @Nullable
    PlayerCorpse getDeadBody(UUID playerOwner);

    /**
     * Kill a player.
     */
    void killPlayer(@NotNull Player killer, @NotNull Player victim);

    /**
     * Get kill distance.
     */
    double getKillDistance();

    /**
     * Change kill distance.
     */
    void setKillDistance(double distance);

    /**
     * Set kill delay.
     */
    void setKillDelay(int seconds);

    /**
     * Get kill delay.
     */
    int getKillDelay();

    /**
     * Add an active sabotage to the arena.
     */
    void addSabotage(SabotageBase sabotageBase);

    /**
     * Remove a finished sabotage.
     */
    void removeSabotage(SabotageBase sabotageBase);

    /**
     * Get active sabotages.
     */
    List<SabotageBase> getLoadedSabotages();

    /**
     * Interrupt players from current tasks.
     */
    void interruptTasks();

    /**
     * Interrupt player from current tasks.
     */
    void interruptTasks(Player player);

    /**
     * Check if the given sabotage is an active sabotage.
     *
     * @param identifier sabotage id.
     */
    boolean hasLoadedSabotage(String identifier);

    /**
     * Get loaded sabotage if exists.
     */
    @Nullable SabotageBase getLoadedSabotage(String provider, String sabotageId);

    /**
     * Register a listener.
     */
    void registerGameListener(GameListener listener);

    /**
     * Unregister a game listener.
     */
    void unRegisterGameListener(GameListener listener);

    /**
     * Game listeners.
     */
    LinkedList<GameListener> getGameListeners();

    /**
     * Check if doing tasks is allowed at this time.
     * Because some sabotages do not allow that.
     */
    boolean isTasksAllowedATM();

    /**
     * Disable tasks indicators if they aren't disabled yet.
     * This is usually triggered during sabotages.
     */
    void disableTaskIndicators();

    /**
     * Enable task indicators back if there isn't an active sabotage blocking this action.
     * Task indicators are blocked by {@link #isTasksAllowedATM()}.
     *
     * @return true if could enable back. False if already enabled or cannot be enabled.
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean tryEnableTaskIndicators();

    /**
     * Add a game room.
     */
    void addRoom(GameRoom room);

    /**
     * Remove a game room.
     */
    void removeRoom(GameRoom room);

    /**
     * Get player current room.
     */
    @Nullable GameRoom getPlayerRoom(Player player);

    /**
     * Get current room.
     */
    @Nullable GameRoom getRoom(Location location);

    void defeatBySabotage(String reasonPath);


    /**
     * Get arena vent handler.
     */
    @Nullable
    VentHandler getVentHandler();

    /**
     * Set vent manager.
     */
    void setVentHandler(@Nullable VentHandler ventingHandler);
}
