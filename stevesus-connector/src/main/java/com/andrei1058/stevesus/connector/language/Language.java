package com.andrei1058.stevesus.connector.language;

import com.andrei1058.stevesus.common.hook.HookManager;
import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("unused")
public class Language extends YamlConfiguration implements CommonLocale {

    private YamlConfiguration yml;
    private final String isoCode;
    private File languageFile;
    private String prefix = "";

    protected Language(String isoCode) {
        this.isoCode = isoCode;
        reload();
    }

    @Override
    public String getMsg(@Nullable Player player, CommonMessage message) {
        return getMsg(player, message.toString());
    }

    @Override
    public String getMsg(@Nullable CommandSender sender, CommonMessage message) {
        if (sender instanceof Player) {
            return getMsg((Player) sender, message);
        }
        return getMsg(null, message);
    }

    @Override
    public List<String> getMsgList(@Nullable Player player, CommonMessage message) {
        return getMsgList(player, message.toString());
    }

    @Override
    public List<String> getMsgList(@Nullable Player player, CommonMessage path, @Nullable String[] replacements) {
        return getMsgList(player, path.toString(), replacements);
    }

    @Override
    public void setMsg(String path, String value) {
        yml.set(path, value);
        save();
    }

    @Override
    public void setList(String path, List<String> value) {
        yml.set(path, value);
        save();
    }

    @Override
    public boolean hasPath(String path) {
        return yml.get(path) != null;
    }

    @Override
    public String getMsg(@Nullable Player player, String path) {
        Object o = getYml().get(path);
        if (o == null && !(this instanceof FallBackLanguage)) {
            return LanguageManager.getINSTANCE().getDefaultLocale().getMsg(player, path);
        }
        if (o instanceof String) {
            return ChatColor.translateAlternateColorCodes('&', replacePlaceholders(player, (String) o));
        } else if (o instanceof List) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Object s : ((List<?>) o)) {
                stringBuilder.append(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(player, s.toString()))).append("\n");
            }
            return stringBuilder.toString();
        }
        return o == null ? "" : o.toString();
    }

    @Override
    public List<String> getMsgList(@Nullable Player player, String path) {
        return getMsgList(player, path, null);
    }

    public List<String> getMsgList(@Nullable Player player, String path, String[] replacements) {
        Object o = getYml().get(path);
        if (o == null && !(this instanceof FallBackLanguage)) {
            return LanguageManager.getINSTANCE().getDefaultLocale().getMsgList(player, path, replacements);
        }
        if (o instanceof List) {
            List<String> list = new ArrayList<>();

            if (replacements != null && replacements.length > 1) {
                for (Object s : ((List<?>) o)) {
                    String toAdd = s.toString();
                    for (int i = 0; i < replacements.length; i += 2) {
                        toAdd = toAdd.replace(replacements[i], replacements[i + 1]);
                    }
                    list.add(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(player, toAdd)));
                }
                return list;
            }

            ((List<?>) o).forEach(s -> list.add(ChatColor.translateAlternateColorCodes('&', replacePlaceholders(player, s.toString()))));
            return list;
        }
        return o == null ? new ArrayList<>() : Collections.singletonList(o.toString());
    }

    @Override
    public String getIsoCode() {
        return isoCode;
    }

    @Override
    public boolean isEnabled() {
        return getYml().getBoolean(CommonMessage.ENABLE.toString());
    }

    public void reload() {
        languageFile = new File(LanguageManager.getINSTANCE().getLocalesFolder(), "messages_" + isoCode + ".yml");
        if (!languageFile.exists()) {
            SteveSusConnector.getInstance().getLogger().info("Creating: " + languageFile.getPath());
            try {
                if (!languageFile.createNewFile()) {
                    if (this instanceof FallBackLanguage) {
                        SteveSusConnector.getInstance().getLogger().severe("Could not create default language file: " + languageFile.getPath());
                    } else {
                        SteveSusConnector.getInstance().getLogger().severe("Could not create file: " + languageFile.getPath());
                    }
                }
            } catch (IOException e) {
                SteveSusConnector.getInstance().getLogger().severe("Could not create file: " + languageFile.getPath());
            }
        }
        yml = YamlConfiguration.loadConfiguration(languageFile);

        prefix = hasPath(CommonMessage.PREFIX.toString()) ? ChatColor.translateAlternateColorCodes('&', yml.getString(CommonMessage.PREFIX.toString())) :
                LanguageManager.getINSTANCE().getDefaultLocale() == null ? "" : LanguageManager.getINSTANCE().getDefaultLocale().getMsg(null, CommonMessage.PREFIX);
    }

    public String getPrefix() {
        return prefix;
    }

    public YamlConfiguration getYml() {
        return yml;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof CommonLocale) {
            return ((CommonLocale) o).getIsoCode().equals(getIsoCode());
        }
        return false;
    }

    protected void addDefault(CommonMessage message, Object value) {
        getYml().addDefault(message.toString(), value);
    }

    public void save() {
        try {
            getYml().save(languageFile);
        } catch (IOException e) {
            //LanguageManager.getLogger().warn("Could not save language: " + getIsoCode(), e);
        }
    }

    @Override
    public String getRawMsg(String path) {
        Object o = getYml().get(path);
        if (o == null && !(this instanceof FallBackLanguage)) {
            return LanguageManager.getINSTANCE().getDefaultLocale().getRawMsg(path);
        }
        if (o instanceof String) {
            return (String) o;
        } else if (o instanceof List) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Object s : ((List<?>) o)) {
                stringBuilder.append(s.toString()).append("\n");
            }
            return stringBuilder.toString();
        }
        return o == null ? "" : o.toString();
    }

    @Override
    public List<String> getRawList(String path) {
        Object o = getYml().get(path);
        if (o == null && !(this instanceof FallBackLanguage)) {
            return LanguageManager.getINSTANCE().getDefaultLocale().getRawList(path);
        }
        if (o instanceof List<?>) {
            //noinspection unchecked
            return (List<String>) o;
        }
        return o == null ? new ArrayList<>() : Collections.singletonList(o.toString());
    }

    @Override
    public String formatDate(@Nullable Date date) {
        if (date == null) return getMsg(null, CommonMessage.DATE_NONE);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getRawMsg(CommonMessage.DATE_FORMAT.toString()));
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(getRawMsg(CommonMessage.TIME_ZONE.toString())));
        return simpleDateFormat.format(date);
    }

    /**
     * Replace player placeholders in given message.
     */
    protected String replacePlaceholders(Player player, String message) {
        return HookManager.getInstance().getPapiHook().parsePlaceholders(player, message.replace("{vault_prefix}",
                HookManager.getInstance().getVaultChatHook().getPlayerPrefix(player)).replace("{vault_suffix}",
                HookManager.getInstance().getVaultChatHook().getPlayerSuffix(player))).replace("{prefix}", getPrefix())
                .replace("{server_name}", SteveSusConnector.getServerName());
    }
}
