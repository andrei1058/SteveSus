package dev.andrei1058.game.common.hook.vault.econ;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class VaultEconSupport implements VaultEconHook {
    private final Economy econ;

    public VaultEconSupport(Economy provider) {
        this.econ = provider;
    }

    @Override
    public ActionResponse bankBalance(String name) {
        return convertResponse(econ.bankBalance(name));
    }

    @Override
    public ActionResponse bankDeposit(String name, double amount) {
        return convertResponse(econ.bankDeposit(name, amount));
    }

    @Override
    public ActionResponse bankHas(String name, double amount) {
        return convertResponse(econ.bankHas(name, amount));
    }

    @Override
    public ActionResponse bankWithdraw(String name, double amount) {
        return convertResponse(econ.bankWithdraw(name, amount));
    }

    @Override
    public ActionResponse createBank(String name, OfflinePlayer player) {
        return convertResponse(econ.createBank(name, player));
    }

    @Override
    public String currencyNamePlural() {
        return econ.currencyNamePlural();
    }

    @Override
    public String currencyNameSingular() {
        return econ.currencyNameSingular();
    }

    @Override
    public ActionResponse deleteBank(String name) {
        return convertResponse(econ.deleteBank(name));
    }

    @Override
    public ActionResponse depositPlayer(OfflinePlayer player, double amount) {
        return convertResponse(econ.depositPlayer(player, amount));
    }

    @Override
    public ActionResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return convertResponse(econ.depositPlayer(player, worldName, amount));
    }

    @Override
    public String format(double amount) {
        return econ.format(amount);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return econ.getBalance(player);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return econ.getBalance(player, world);
    }

    @Override
    public List<String> getBanks() {
        return econ.getBanks();
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return econ.has(player, amount);
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return econ.has(player, worldName, amount);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return econ.hasAccount(player);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return econ.hasAccount(player, worldName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return econ.createPlayerAccount(player);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return econ.createPlayerAccount(player, worldName);
    }

    @Override
    public boolean hasBankSupport() {
        return econ.hasBankSupport();
    }

    @Override
    public ActionResponse isBankMember(String name, OfflinePlayer player) {
        return convertResponse(econ.isBankMember(name, player));
    }

    @Override
    public ActionResponse isBankOwner(String name, OfflinePlayer player) {
        return convertResponse(econ.isBankOwner(name, player));
    }

    @Override
    public ActionResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return convertResponse(econ.withdrawPlayer(player, amount));
    }

    @Override
    public ActionResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return convertResponse(econ.withdrawPlayer(player, worldName, amount));
    }

    /**
     * Convert economy response to local response.
     */
    private ActionResponse convertResponse(EconomyResponse economyResponse) {
        return new ActionResponse(VaultEconHook.ActionResponse.Type.valueOf(economyResponse.type.name()), economyResponse.transactionSuccess(), economyResponse.amount, economyResponse.balance, economyResponse.errorMessage);
    }
}
