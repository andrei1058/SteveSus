package dev.andrei1058.game.common.hook;

import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.hook.papi.fetcher.PAPIFallBack;
import dev.andrei1058.game.common.hook.papi.fetcher.PAPIHook;
import dev.andrei1058.game.common.hook.papi.fetcher.PAPISupport;
import dev.andrei1058.game.common.hook.papi.provider.AdditionalParser;
import dev.andrei1058.game.common.hook.papi.provider.PAPIProvider;
import dev.andrei1058.game.common.hook.vault.chat.NoChatSupport;
import dev.andrei1058.game.common.hook.vault.chat.VaultChatHook;
import dev.andrei1058.game.common.hook.vault.chat.VaultChatSupport;
import dev.andrei1058.game.common.hook.vault.econ.NoEconSupport;
import dev.andrei1058.game.common.hook.vault.econ.VaultEconHook;
import dev.andrei1058.game.common.hook.vault.econ.VaultEconSupport;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

/**
 * Hooks are external plugins integration/ support.
 */
@SuppressWarnings("unused")
public class HookManager {

    private static HookManager instance;


    private VaultChatHook vaultChatHook = new NoChatSupport();
    private VaultEconHook vaultEconHook = new NoEconSupport();
    private PAPIHook papiHook = new PAPIFallBack();

    private HookManager(boolean vault, @Nullable String papiIdentifier, @Nullable AdditionalParser additionalParser) {
        instance = this;

        // Vault support. Soft-depend.
        if (Bukkit.getPluginManager().isPluginEnabled("Vault") && vault) {
            RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
            if (chatProvider != null) {
                vaultChatHook = new VaultChatSupport(chatProvider.getProvider());
                CommonManager.getINSTANCE().getPlugin().getLogger().info("Hook: Vault -> Chat.");
            }
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                vaultEconHook = new VaultEconSupport(economyProvider.getProvider());
                CommonManager.getINSTANCE().getPlugin().getLogger().info("Hook: Vault -> Economy.");
            }
        }

        // PlaceholderAPI support. Soft-depend.
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && papiIdentifier != null) {
            papiHook = new PAPISupport();
            CommonManager.getINSTANCE().getPlugin().getLogger().info("Hook: PlaceholderAPI -> placeholder parser.");
            // register expansion as well
            if (new PAPIProvider(papiIdentifier, additionalParser).register()) {
                CommonManager.getINSTANCE().getPlugin().getLogger().info("Hook: PlaceholderAPI -> registered extension.");
            }
        }
    }

    /**
     * Get vault chat support.
     * If vault was not installed will use an empty interface
     * so it won't break the plugin.
     */
    public VaultChatHook getVaultChatHook() {
        return vaultChatHook;
    }

    /**
     * Get vault economy support.
     * If no economy found it will use an empty interface
     * so it won't break the plugin.
     */
    public VaultEconHook getVaultEconHook() {
        return vaultEconHook;
    }

    /**
     * Get placeholder API hook.
     * If dependency is not loaded will use an empty interface
     * so it won't break the plugin.
     */
    public PAPIHook getPapiHook() {
        return papiHook;
    }

    /**
     * Initialize hooks manager in your plugin's onEnable.
     * <p>
     * Required soft-depends: Vault, PlaceholderAPI.
     *
     * @param vault            true if should check for vault hook.
     * @param papiIdentifier   null if you do not want to use PAPI support, placeholder root identifier otherwise.
     * @param additionalParser additional placeholders providers for current module if PAPI support is enabled. Null to ignore.
     */
    public static void onEnable(boolean vault, @Nullable String papiIdentifier, @Nullable AdditionalParser additionalParser) {
        if (instance == null) {
            new HookManager(vault, papiIdentifier, additionalParser);
        }
    }

    /**
     * Get hooks manager.
     */
    public static HookManager getInstance() {
        return instance;
    }
}
