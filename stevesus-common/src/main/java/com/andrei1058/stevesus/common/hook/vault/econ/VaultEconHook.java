package com.andrei1058.stevesus.common.hook.vault.econ;

import org.bukkit.OfflinePlayer;

import java.util.List;

@SuppressWarnings("ALL")
public interface VaultEconHook {

    class ActionResponse {
        enum Type {
            FAILURE, NOT_IMPLEMENTED, SUCCESS;
        }

        boolean transactionSuccess;
        double amount;
        double balance;
        String errorMessage = "";
        Type type;

        protected ActionResponse(Type type, boolean transactionSuccess, double amount, double balance, String errorMessage) {
            this.type = type;
            this.transactionSuccess = transactionSuccess;
            this.amount = amount;
            this.balance = balance;
            this.errorMessage = errorMessage;
        }

        /**
         * Checks if an operation was successful.
         */
        public boolean isTransactionSuccess() {
            return transactionSuccess;
        }

        /**
         * Amount modified by calling method.
         */
        public double getAmount() {
            return amount;
        }

        /**
         * New balance of account.
         */
        public double getBalance() {
            return balance;
        }

        /**
         * Error message if the variable 'type' is FAILURE.
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Type indicates whether the plugin currently being used for Economy actually allows the method, or if the operation was a success or failure.
         */
        public Type getType() {
            return type;
        }
    }

    /**
     * Returns the amount the bank has.
     */
    ActionResponse bankBalance(String name);

    /**
     * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS.
     */
    ActionResponse bankDeposit(String name, double amount);

    /**
     * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS.
     */
    ActionResponse bankHas(String name, double amount);

    /**
     * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS.
     */
    ActionResponse bankWithdraw(String name, double amount);

    /**
     * Creates a bank account with the specified name and the player as the owner.
     */
    ActionResponse createBank(String name, OfflinePlayer player);

    /**
     * Returns the name of the currency in plural form.
     */
    String currencyNamePlural();

    /**
     * Returns the name of the currency in singular form.
     */
    String currencyNameSingular();

    /**
     * Deletes a bank account with the specified name.
     */
    ActionResponse deleteBank(String name);

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS.
     */
    ActionResponse depositPlayer(OfflinePlayer player, double amount);

    /**
     * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     */
    ActionResponse depositPlayer(OfflinePlayer player, String worldName, double amount);

    /**
     * Format amount into a human readable String This provides translation into economy specific formatting to improve consistency between plugins.
     */
    String format(double amount);

    /**
     * Gets balance of a player.
     */
    double getBalance(OfflinePlayer player);

    /**
     * Gets balance of a player on the specified world.
     */
    double getBalance(OfflinePlayer player, String world);

    /**
     * Gets the list of banks.
     */
    List<String> getBanks();

    /**
     * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS.
     */
    boolean has(OfflinePlayer player, double amount);

    /**
     * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
     */
    boolean has(OfflinePlayer player, String worldName, double amount);

    /**
     * Checks if this player has an account on the server yet.
     * If false you may want to give a look at {@link #createPlayerAccount(OfflinePlayer)}.
     * This will always return true if the player has joined the server at least once as all major economy plugins auto-generate a player account when the player joins the server.
     */
    boolean hasAccount(OfflinePlayer player);

    /**
     * Checks if this player has an account on the server yet on the given world.
     * If false you may want to give a look at {@link #createPlayerAccount(OfflinePlayer, String)}.
     * This will always return true if the player has joined the server at least once as all major economy plugins auto-generate a player account when the player joins the server.
     */
    boolean hasAccount(OfflinePlayer player, String worldName);

    /**
     * Attempts to create a player account for the given player.
     */
    boolean createPlayerAccount(OfflinePlayer player);

    /**
     * Attempts to create a player account for the given player on the specified world IMPLEMENTATION SPECIFIC - if an economy plugin does not support this then false will always be returned.
     */
    boolean createPlayerAccount(OfflinePlayer player, String worldName);

    /**
     * Returns true if the given implementation supports banks.
     */
    boolean hasBankSupport();

    /**
     * Check if the player is a member of the bank account.
     */
    ActionResponse isBankMember(String name, OfflinePlayer player);

    /**
     * Check if a player is the owner of a bank account.
     */
    ActionResponse isBankOwner(String name, OfflinePlayer player);

    /**
     * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS.
     */
    ActionResponse withdrawPlayer(OfflinePlayer player, double amount);

    /**
     * Withdraw an amount from a player on a given world
     * - DO NOT USE NEGATIVE AMOUNTS IMPLEMENTATION SPECIFIC -
     * if an economy plugin does not support this the global balance will be returned.
     */
    ActionResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount);
}
