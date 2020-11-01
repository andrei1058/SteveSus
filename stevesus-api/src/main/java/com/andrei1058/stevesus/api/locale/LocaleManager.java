package com.andrei1058.stevesus.api.locale;

import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

public interface LocaleManager {

    /**
     * Get a player language.
     *
     * @param player target player.
     * @return player language of if he does not have any, return default language.
     */
    @NotNull
    Locale getLocale(@NotNull Player player);

    /**
     * Get a player language.
     *
     * @param player target player.
     * @return player language of if he does not have any, return default language.
     */
    @NotNull
    Locale getLocale(UUID player);

    /**
     * Get a message in player's language.
     *
     * @param player  target player.
     * @param message message.
     * @return Chat color translated message in player's language.
     */
    String getMsg(@NotNull Player player, Message message);

    /**
     * Get a message in command sender's language.
     *
     * @param sender  command sender.
     * @param message message.
     * @return Chat color translated message in command sender's language.
     */
    String getMsg(@NotNull CommandSender sender, CommonMessage message);

    /**
     * Get a message in player's language.
     *
     * @param player  target player.
     * @param message message.
     * @return Chat color translated message in player's language.
     */
    String getMsg(@NotNull Player player, CommonMessage message);

    /**
     * Get a message in command sender's language.
     *
     * @param sender  command sender.
     * @param message message.
     * @return Chat color translated message in command sender's language.
     */
    @SuppressWarnings("unused")
    String getMsg(@NotNull CommandSender sender, Message message);

    /**
     * Get language folder.
     * This is where language files are retrieved from.
     */
    File getLocalesFolder();

    /**
     * Add a new language to the enabled languages list.
     *
     * @param translation language to be registered.
     * @return true if successfully added.
     * Will return false if could not validate messages paths.
     * It may return false if adding this language or languages in general is not allowed.
     * Will return false if language is already loaded for example.
     * Will return false if language is disabled from config.
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    boolean addLocale(Locale translation);

    /**
     * Remove a language from the languages list.
     *
     * @param translation language to be removed.
     * @return true if removed successfully.
     * Will return false if given language is the default language.
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    boolean removeLocale(Locale translation);

    /**
     * Get server default language.
     *
     * @return server default locale.
     */
    Locale getDefaultLocale();

    /**
     * Change a player language.
     * It is not recommended to do it during the game.
     * The player must be online. If you want to "pre-load" player languages use
     * your own system and add the player here only when he is online.
     * WILL SAVE THE LANGUAGE TO THE DATABASE AS WELL (ASYNC).
     *
     * @param uuid         player uuid. He must be online.
     * @param translation  language. Null to restore to default server language. If the language is not registered will return false.
     * @param triggerEvent true if you want to trigger language change event. Usually false at join.
     * @return true if switched successfully. False if language is not registered,
     * if uuid is not on the server, if switch not allowed at this moment or other.
     */
    boolean setPlayerLocale(UUID uuid, @Nullable Locale translation, boolean triggerEvent);

    /**
     * Get loaded languages.
     * This list cannot be modified.
     *
     * @return loaded languages.
     */
    List<Locale> getEnabledLocales();

    /**
     * Set server default language.
     *
     * @param translation language.
     * @return true if set successfully.
     * Will return false if the language is not registered,
     * if cannot be validated or other.
     */
    @SuppressWarnings("unused")
    boolean setDefaultLocale(Locale translation);

    /**
     * Get a translation by iso code.
     *
     * @param isoCode language iso code.
     * @return null if not found.
     */
    @Nullable
    Locale getLocale(String isoCode);

    /**
     * Check if a language exists (is enabled and loaded) by its iso code.
     *
     * @param isoCode iso code to be checked.
     * @return true if translation exists.
     */
    @SuppressWarnings("unused")
    boolean isLocaleExist(@Nullable String isoCode);

    /**
     * Format a date using player's time-zone.
     *
     * @param player player used to retrieve time-zone.
     * @param date   date to be formatted.
     */
    String formatDate(Player player, @Nullable Date date);
}
