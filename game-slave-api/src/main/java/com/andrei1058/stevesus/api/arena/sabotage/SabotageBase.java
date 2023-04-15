package com.andrei1058.stevesus.api.arena.sabotage;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SabotageBase {

    /**
     * Check if sabotage was fixed.
     */
    public abstract boolean isActive();

    /**
     * Start emergency.
     */
    public abstract void activate(@Nullable Player player);

    /**
     * Get sabotage provider. (parent)
     */
    public abstract @NotNull SabotageProvider getProvider();

    /**
     * Can use emergency button during this sabotage?
     * Only if this is active.
     */
    public boolean isEmergencyButtonAllowed() {
        return false;
    }

    /**
     * Can report dead body during this sabotage?
     * Only if active.
     */
    public boolean canReportDeadBody() {
        return true;
    }

    /**
     * Can players do tasks during this sabotage?
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isAllowTasks() {
        return false;
    }
}
