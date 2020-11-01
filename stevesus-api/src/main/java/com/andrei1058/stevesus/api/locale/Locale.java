package com.andrei1058.stevesus.api.locale;

import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public interface Locale extends CommonLocale {

    /**
     * Color color translated message and
     * placeholder replacements.
     *
     * @param path   message path.
     * @param player used to parse placeholders. Nullable.
     * @return Chat color translated message at given path.
     */
    String getMsg(@Nullable Player player, String path);


    /**
     * Color color translated message.
     *
     * @param message message.
     * @param sender  if requesting a player message. Used to parse placeholders.
     * @return Chat color translated message at given path.
     */
    default String getMsg(@Nullable CommandSender sender, CommonMessage message) {
        if (sender instanceof Player) {
            return getMsg((Player) sender, message);
        }
        return getMsg(null, message);
    }

    /**
     * Get a raw message. No color translation.
     * No placeholder parsing.
     */
    String getRawMsg(String path);

    /**
     * Get a raw message list. No color translation.
     * No placeholder parsing.
     */
    List<String> getRawList(String path);

    /**
     * Color color translated message.
     *
     * @param message message.
     * @param player  used to parse placeholders. Nullable.
     * @return Chat color translated message at given path.
     */
    default String getMsg(@Nullable Player player, Message message) {
        return getMsg(player, message.toString());
    }

    /**
     * Color color translated message.
     *
     * @param message message.
     * @param player  used to parse placeholders. Nullable.
     * @return Chat color translated message at given path.
     */
    default String getMsg(@Nullable Player player, CommonMessage message) {
        return getMsg(player, message.toString());
    }

    /**
     * Color color translated message.
     *
     * @param path   message path.
     * @param player player used to parse placeholders. Nullable.
     * @return Chat color translated message at given path.
     */
    default List<String> getMsgList(@Nullable Player player, String path) {
        return getMsgList(player, path, null);
    }

    /**
     * Color color translated message.
     *
     * @param path         message path.
     * @param player       player used to retrieve placeholders. Nullable.
     * @param replacements nullable.
     * @return Chat color translated message at given path.
     */
    List<String> getMsgList(@Nullable Player player, String path, String[] replacements);

    /**
     * Color color translated message.
     *
     * @param path   message path.
     * @param player player used to parse placeholders. Nullable.
     * @return Chat color translated message at given path.
     */
    default List<String> getMsgList(@Nullable Player player, CommonMessage path, @Nullable String[] replacements) {
        return getMsgList(player, path.toString(), replacements);
    }

    /**
     * Color color translated message.
     *
     * @param message message.
     * @param player  player used to parse placeholders. Nullable.
     * @return Chat color translated message at given path.
     */
    @SuppressWarnings("unused")
    default List<String> getMsgList(@Nullable Player player, Message message) {
        return getMsgList(player, message.toString());
    }

    /**
     * Color color translated message.
     *
     * @param message message.
     * @param player  player used to parse placeholders. Nullable.
     * @return Chat color translated message at given path.
     */
    default List<String> getMsgList(@Nullable Player player, CommonMessage message) {
        return getMsgList(player, message.toString());
    }

    /**
     * Get language iso code.
     * Languages are identified by this code.
     */
    String getIsoCode();

    /**
     * Reload translation file.
     */
    void reload();

    /**
     * Format date.
     *
     * @param date date to be formatted;
     */
    String formatDate(@Nullable Date date);

    /**
     * Get date format with time zone applied.
     */
    SimpleDateFormat getTimeZonedDateFormat();
}
