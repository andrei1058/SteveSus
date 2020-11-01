package com.andrei1058.stevesus.common.hook.vault.econ;

import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;

public class NoEconSupport implements VaultEconHook {

    private static final ActionResponse NOT_IMPLEMENTED = new ActionResponse(ActionResponse.Type.NOT_IMPLEMENTED, false, 0, 0, "Action not supported!");

    @Override
    public ActionResponse bankBalance(String name) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse bankDeposit(String name, double amount) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse bankHas(String name, double amount) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse bankWithdraw(String name, double amount) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse createBank(String name, OfflinePlayer player) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public String currencyNamePlural() {
        return "Euros";
    }

    @Override
    public String currencyNameSingular() {
        return "Euro";
    }

    @Override
    public ActionResponse deleteBank(String name) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse depositPlayer(OfflinePlayer player, double amount) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public String format(double amount) {
        return String.valueOf(amount);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return 0;
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return 0;
    }

    @Override
    public List<String> getBanks() {
        return Collections.emptyList();
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return false;
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public ActionResponse isBankMember(String name, OfflinePlayer player) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse isBankOwner(String name, OfflinePlayer player) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return NOT_IMPLEMENTED;
    }

    @Override
    public ActionResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return NOT_IMPLEMENTED;
    }
}
