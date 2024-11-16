package com.andrei1058.stevesus.connector.language;

import com.andrei1058.dbi.DatabaseAdapter;
import com.andrei1058.dbi.column.datavalue.SimpleValue;
import com.andrei1058.stevesus.common.api.locale.CommonLocale;
import com.andrei1058.stevesus.common.api.locale.CommonLocaleManager;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.stevesus.common.database.DatabaseManager;
import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.connector.api.event.PlayerLanguageChangeEvent;
import com.andrei1058.stevesus.connector.config.ConnectorConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Date;
import java.util.*;

public class LanguageManager implements CommonLocaleManager {

    private static LanguageManager INSTANCE;

    private final LinkedList<CommonLocale> loadedLanguages = new LinkedList<>();
    private final HashMap<UUID, CommonLocale> languageByPlayer = new HashMap<>();
    private CommonLocale defaultLanguage;
    private File languagesFolder = new File(SteveSusConnector.getInstance().getDataFolder(), "Locales");

    private LanguageManager() {
    }

    /**
     * Initialize Language Manager.
     */
    public static void onLoad() {
        if (INSTANCE != null) return;
        INSTANCE = new LanguageManager();

        // change languages directory if needed
        String languagesPath = SteveSusConnector.getConnectorConfig().getProperty(ConnectorConfig.LANGUAGES_FOLDER);
        if (!languagesPath.isEmpty()) {
            File newLanguagesFolder = new File(languagesPath);
            if (!newLanguagesFolder.isDirectory()) {
                SteveSusConnector.getInstance().getLogger().severe("Tried to set languages path to: " + newLanguagesFolder + " but it does not seem to be a directory.");
            } else {
                getINSTANCE().languagesFolder = newLanguagesFolder;
                SteveSusConnector.getInstance().getLogger().info("Set languages path to: " + newLanguagesFolder + ".");
            }
        }

        // create languages folder
        if (!INSTANCE.getLocalesFolder().exists()) {
            if (!INSTANCE.getLocalesFolder().mkdir()) {
                SteveSusConnector.getInstance().getLogger().severe("Could not create directory: " + INSTANCE.getLocalesFolder().getPath());
            }
        }

        // define default language
        FallBackLanguage fallBackLanguage = new FallBackLanguage();
        FallBackLanguage.initDefaultMessages(fallBackLanguage);
        // add it to the list first
        INSTANCE.addLocale(fallBackLanguage);
        // then set
        INSTANCE.setDefaultLocale(fallBackLanguage);

        // load other languages
        for (File inFolder : getINSTANCE().getLocalesFolder().listFiles()) {
            if (inFolder == null) continue;
            if (!inFolder.isFile()) continue;
            if (!inFolder.getName().endsWith(".yml")) continue;
            if (!inFolder.getName().startsWith("messages_")) continue;
            Language language = new Language(inFolder.getName().replace("messages_", "").replace(".yml", ""));
            getINSTANCE().addLocale(language);
        }

        // set default language from config
        String defaultIso = SteveSusConnector.getConnectorConfig().getProperty(ConnectorConfig.FALLBACK_LANGUAGE);
        CommonLocale defaultLanguage = INSTANCE.getEnabledCommonLocales().stream().filter(lang -> lang.getIsoCode().equals(defaultIso)).findFirst().orElse(null);
        if (defaultLanguage != null && getINSTANCE().setDefaultLocale(defaultLanguage)) {
            SteveSusConnector.getInstance().getLogger().info("Set " + defaultIso + " as server's default language!");

            // unload fallback language if disabled
            if (!fallBackLanguage.getBoolean(CommonMessage.ENABLE.toString())) {
                getINSTANCE().removeLocale(fallBackLanguage);
            }
        } else {
            SteveSusConnector.getInstance().getLogger().severe("Tried to set language " + defaultIso + " as server's default but it seems invalid!");
        }
    }

    public static void onEnable() {
        DatabaseManager.getINSTANCE().getDatabase().createUserLanguageTable();
    }

    /**
     * Get Language Manager Instance.
     */
    public static LanguageManager getINSTANCE() {
        return INSTANCE;
    }

    @Override
    public @NotNull CommonLocale getLocale(@NotNull Player player) {
        return getINSTANCE().languageByPlayer.getOrDefault(player.getUniqueId(), getINSTANCE().defaultLanguage);
    }

    public @NotNull CommonLocale getLocale(@NotNull CommandSender player) {
        if (player instanceof Player) {
            return getINSTANCE().languageByPlayer.getOrDefault(((Player) player).getUniqueId(), getINSTANCE().defaultLanguage);
        }
        return getINSTANCE().getDefaultLocale();
    }

    @Override
    public @NotNull CommonLocale getLocale(UUID player) {
        return getINSTANCE().languageByPlayer.getOrDefault(player, getINSTANCE().defaultLanguage);
    }

    @Override
    public String getMsg(@NotNull Player player, CommonMessage message) {
        return getLocale(player).getMsg(player, message);
    }

    @Override
    public String getMsg(@NotNull CommandSender sender, CommonMessage message) {
        if (sender instanceof Player) {
            return getMsg((Player) sender, message);
        }
        return defaultLanguage.getMsg(sender, message);
    }

    @Override
    public File getLocalesFolder() {
        return languagesFolder;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean addLocale(CommonLocale translation) {
        if (!translation.hasPath(CommonMessage.NAME.toString())) return false;
        if (loadedLanguages.stream().anyMatch(lang -> lang.equals(translation))) return false;
        if (!translation.isEnabled()) return false;
        SteveSusConnector.debug("Adding language: " + translation.getMsg(null, CommonMessage.NAME));
        return loadedLanguages.add(translation);
    }

    @Override
    public boolean removeLocale(CommonLocale translation) {
        if (translation.getIsoCode().equals(defaultLanguage.getIsoCode())) {
            return false;
        }
        // players to be switched to default language
        LinkedList<UUID> toSwitch = new LinkedList<>();
        languageByPlayer.forEach((uuid, translation1) -> {
            if (translation.equals(translation1)) {
                toSwitch.add(uuid);
            }
        });
        toSwitch.forEach(uuid -> {
            if (!setPlayerLocale(uuid, getDefaultLocale(), true)) {
                languageByPlayer.remove(uuid);
            }
        });
        boolean result = loadedLanguages.remove(translation);
        if (result) {
            SteveSusConnector.debug("Disabled language: " + translation.getMsg(null, CommonMessage.NAME));
        }
        return result;
    }

    @Override
    public CommonLocale getDefaultLocale() {
        return defaultLanguage;
    }

    @Override
    public boolean setPlayerLocale(UUID uuid, CommonLocale translation, boolean triggerEvent) {
        CommonLocale old = LanguageManager.getINSTANCE().getLocale(uuid);
        if (translation == null) {
            languageByPlayer.remove(uuid);
            if (triggerEvent) {
                if (!old.equals(INSTANCE.getDefaultLocale())) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        Bukkit.getPluginManager().callEvent(new PlayerLanguageChangeEvent(player, INSTANCE.getDefaultLocale(), old));
                    }
                }
            }
            return true;
        }
        if (loadedLanguages.stream().noneMatch(translation1 -> translation1.equals(translation))) return false;
        if (old.equals(INSTANCE.getDefaultLocale())) return false;

        if (translation.equals(getDefaultLocale())) {
            languageByPlayer.remove(uuid);
        } else {
            if (languageByPlayer.containsKey(uuid)) {
                languageByPlayer.replace(uuid, translation);
            } else {
                languageByPlayer.put(uuid, translation);
            }
        }
        if (triggerEvent) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                Bukkit.getPluginManager().callEvent(new PlayerLanguageChangeEvent(player, translation, old));
            }
        }

        DatabaseManager.getINSTANCE().getDatabase().saveUserLanguage(uuid, translation);
        return true;
    }

    @Override
    public List<CommonLocale> getEnabledCommonLocales() {
        return Collections.unmodifiableList(loadedLanguages);
    }

    @Override
    public boolean setDefaultLocale(CommonLocale translation) {
        if (loadedLanguages.stream().noneMatch(lang -> lang.equals(translation))) return false;
        if (translation instanceof Language) {
            FallBackLanguage.initDefaultMessages((Language) translation);
        }
        defaultLanguage = translation;
        return true;
    }

    @Override
    public @Nullable CommonLocale getLocale(String isoCode) {
        return loadedLanguages.stream().filter(lang -> lang.getIsoCode().equals(isoCode)).findFirst().orElse(null);
    }

    @Override
    public boolean isLocaleExist(@Nullable String isoCode) {
        return getLocale(isoCode) != null;
    }

    @Override
    public String formatDate(Player player, @Nullable Date date) {
        return languageByPlayer.getOrDefault(player.getUniqueId(), getDefaultLocale()).formatDate(date);
    }
}
