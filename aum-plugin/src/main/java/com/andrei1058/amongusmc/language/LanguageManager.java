package com.andrei1058.amongusmc.language;

import com.andrei1058.amongusmc.AmongUsMc;
import com.andrei1058.amongusmc.api.event.PlayerLanguageChangeEvent;
import com.andrei1058.amongusmc.api.locale.LocaleManager;
import com.andrei1058.amongusmc.api.locale.Message;
import com.andrei1058.amongusmc.api.locale.Locale;
import com.andrei1058.amongusmc.common.hook.HookManager;
import com.andrei1058.amongusmc.config.MainConfig;
import com.andrei1058.amongusmc.server.ServerManager;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocale;
import com.andrei1058.amoungusmc.common.api.locale.CommonLocaleManager;
import com.andrei1058.amoungusmc.common.api.locale.CommonMessage;
import com.andrei1058.amongusmc.common.database.DatabaseManager;
import com.andrei1058.amongusmc.common.database.table.LanguageTable;
import com.andrei1058.dbi.DatabaseAdapter;
import com.andrei1058.dbi.column.datavalue.SimpleValue;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.Date;
import java.util.*;

public class LanguageManager implements LocaleManager, CommonLocaleManager {

    private static LanguageManager INSTANCE;

    private final LinkedList<Locale> loadedLanguages = new LinkedList<>();
    private final HashMap<UUID, Locale> languageByPlayer = new HashMap<>();
    private Locale defaultLanguage;
    private final LanguageTable languageTable = new LanguageTable();
    private File languagesFolder = new File(AmongUsMc.getInstance().getDataFolder(), "Locales");

    private LanguageManager() {
    }

    /**
     * Initialize Language Manager.
     */
    public static void onLoad() {
        if (INSTANCE != null) return;
        INSTANCE = new LanguageManager();

        // change languages directory if needed
        String languagesPath = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.LANGUAGES_FOLDER);
        if (!languagesPath.isEmpty()) {
            File newLanguagesFolder = new File(languagesPath);
            if (!newLanguagesFolder.isDirectory()) {
                AmongUsMc.getInstance().getLogger().severe("Tried to set languages path to: " + newLanguagesFolder + " but it does not seem to be a directory.");
            } else {
                getINSTANCE().languagesFolder = newLanguagesFolder;
                AmongUsMc.getInstance().getLogger().info("Set languages path to: " + newLanguagesFolder + ".");
            }
        }

        // create languages folder
        if (!INSTANCE.getLocalesFolder().exists()) {
            if (!INSTANCE.getLocalesFolder().mkdir()) {
                AmongUsMc.getInstance().getLogger().severe("Could not create directory: " + INSTANCE.getLocalesFolder().getPath());
            }
        }

        // define default language
        FallBackLanguage fallBackLanguage = new FallBackLanguage();
        FallBackLanguage.initDefaultMessages(fallBackLanguage);
        // it needs to be in the list
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
        String defaultIso = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.FALLBACK_LANGUAGE);
        Locale defaultLanguage = INSTANCE.getEnabledLocales().stream().filter(lang -> lang.getIsoCode().equals(defaultIso)).findFirst().orElse(null);
        if (defaultLanguage != null && getINSTANCE().setDefaultLocale(defaultLanguage)) {
            AmongUsMc.getInstance().getLogger().info("Set " + defaultIso + " as server's default language!");

            // unload fallback language if disabled
            if (!fallBackLanguage.getBoolean(CommonMessage.ENABLE.toString())) {
                getINSTANCE().removeLocale(fallBackLanguage);
            }
        } else {
            AmongUsMc.getInstance().getLogger().severe("Tried to set language " + defaultIso + " as server's default but it seems invalid!");
        }
    }

    /**
     * To be used in your plugin's onEnable.
     */
    public static void onEnable() {
        DatabaseManager.getINSTANCE().getDatabase().createTable(getINSTANCE().languageTable, false);
    }

    /**
     * Get Language Manager Instance.
     */
    public static LanguageManager getINSTANCE() {
        return INSTANCE;
    }

    /**
     * Replace player placeholders in given message.
     */
    private String replacePlaceholders(Player player, String message) {
        return HookManager.getInstance().getPapiHook().parsePlaceholders(player, message.replace("{vault_prefix}", HookManager.getInstance().getVaultChatHook().getPlayerPrefix(player)).replace("{vault_suffix}", HookManager.getInstance().getVaultChatHook().getPlayerSuffix(player)));
    }

    public @NotNull Locale getLocale(@NotNull Player player) {
        return getINSTANCE().languageByPlayer.getOrDefault(player.getUniqueId(), getINSTANCE().defaultLanguage);
    }

    @Override
    public CommonLocale getLocale(CommandSender sender) {
        if (sender instanceof Player) {
            return getINSTANCE().languageByPlayer.getOrDefault(((Player) sender).getUniqueId(), getINSTANCE().defaultLanguage);
        } else {
            return getDefaultLocale();
        }
    }

    @Override
    public @NotNull Locale getLocale(UUID player) {
        return getINSTANCE().languageByPlayer.getOrDefault(player, getINSTANCE().defaultLanguage);
    }

    @Override
    public String getMsg(@NotNull Player player, Message message) {
        return replacePlaceholders(player, getLocale(player).getMsg(player, message));
    }

    @Override
    public String getMsg(@NotNull CommandSender sender, Message message) {
        if (sender instanceof Player) {
            return getMsg((Player) sender, message);
        }
        return defaultLanguage.getMsg(null, message);
    }

    @Override
    public List<CommonLocale> getEnabledCommonLocales() {
        return new LinkedList<>(getINSTANCE().getEnabledLocales());
    }

    @Override
    public String getMsg(@NotNull Player player, CommonMessage message) {
        return replacePlaceholders(player, getLocale(player).getMsg(player, message));
    }

    @Override
    public String getMsg(@NotNull CommandSender sender, CommonMessage message) {
        if (sender instanceof Player) {
            return replacePlaceholders((Player) sender, getMsg((Player) sender, message));
        }
        return defaultLanguage.getMsg(null, message);
    }

    @Override
    public File getLocalesFolder() {
        return languagesFolder;
    }

    @Override
    public boolean addLocale(CommonLocale translation) {
        if (!(translation instanceof Locale)) return false;
        return addLocale(((Locale) translation));
    }

    @Override
    public boolean removeLocale(CommonLocale translation) {
        if (!(translation instanceof Locale)) return false;
        return removeLocale(((Locale) translation));
    }

    @Override
    public boolean setPlayerLocale(UUID uuid, @Nullable CommonLocale translation, boolean triggerEvent) {
        if (!(translation instanceof Locale)) return false;
        return setPlayerLocale(uuid, ((Locale) translation), triggerEvent);
    }

    @Override
    public boolean setDefaultLocale(CommonLocale translation) {
        if (!(translation instanceof Locale)) return false;
        return setDefaultLocale(((Locale) translation));
    }

    @Override
    public boolean addLocale(Locale translation) {
        if (!translation.hasPath(CommonMessage.NAME.toString())) return false;
        if (loadedLanguages.stream().anyMatch(lang -> lang.equals(translation))) return false;
        if (!translation.isEnabled()) return false;
        AmongUsMc.debug("Adding language: " + translation.getRawMsg(CommonMessage.NAME.toString()));
        return loadedLanguages.add(translation);
    }

    @Override
    public boolean removeLocale(Locale translation) {
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
            AmongUsMc.debug("Disabled language: " + translation.getMsg(null, CommonMessage.NAME));
        }
        return result;
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLanguage;
    }

    @Override
    public boolean setPlayerLocale(UUID uuid, Locale translation, boolean triggerEvent) {
        Locale old = LanguageManager.getINSTANCE().getLocale(uuid);
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
        LanguageTable table = LanguageManager.getINSTANCE().getLanguageTable();
        DatabaseManager.getINSTANCE().getDatabase().insert(table, Arrays.asList(new SimpleValue<>(table.PRIMARY_KEY, uuid), new SimpleValue<>(table.LANGUAGE, translation)), DatabaseAdapter.InsertFallback.UPDATE);
        return true;
    }

    @Override
    public List<Locale> getEnabledLocales() {
        return Collections.unmodifiableList(loadedLanguages);
    }

    @Override
    public boolean setDefaultLocale(Locale translation) {
        if (loadedLanguages.stream().noneMatch(lang -> lang.equals(translation))) return false;
        if (translation instanceof Language) {
            FallBackLanguage.initDefaultMessages((Language) translation);
        }
        defaultLanguage = translation;
        return true;
    }

    @Override
    public @Nullable Locale getLocale(String isoCode) {
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

    public LanguageTable getLanguageTable() {
        return languageTable;
    }
}
