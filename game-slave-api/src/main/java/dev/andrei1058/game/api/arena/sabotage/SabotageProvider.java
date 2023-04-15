package dev.andrei1058.game.api.arena.sabotage;

import com.andrei1058.spigot.commandlib.ICommandNode;
import com.andrei1058.spigot.commandlib.fast.FastSubRootCommand;
import dev.andrei1058.game.api.SteveSusAPI;
import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.api.setup.SetupSession;
import com.google.gson.JsonObject;
import dev.andrei1058.game.api.locale.Message;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SabotageProvider {

    /**
     * Add your messages defaults etc.
     * <p>
     * Add your messages at this path {@link Message#SABOTAGE_PATH_#toString()} + my-sabotage-identifier-name.
     */
    public abstract void onRegister();

    /**
     * Get plugin provider.
     */
    public abstract Plugin getOwner();

    /**
     * A unique string that will identify your sabotage.
     * Mostly used for language paths.
     */
    public abstract @NotNull String getUniqueIdentifier();

    /**
     * Return sabotage instance if triggered by impostor inventory item.
     * This will automatically add the object to the active sabotages list.
     * If you want to trigger your sabotage manually do not forget to use {@link Arena#addSabotage(SabotageBase)} to start ticking your sabotage.
     *
     * @param arena         arena.
     * @param configuration configuration.
     * @return null if something went wrong or if you do not want to allow this type of trigger.
     */
    public abstract @Nullable SabotageBase onArenaInit(Arena arena, JsonObject configuration);


    @SuppressWarnings("UnstableApiUsage")
    public FastSubRootCommand getMyCommand() {
        ICommandNode myCommand = SteveSusAPI.getInstance().getSetupHandler().getSetSabotageCommand().getSubCommand(getOwner().getName());
        if (myCommand == null) {
            throw new IllegalStateException("Sabotage provider not registered!");
        }
        return (FastSubRootCommand) myCommand;
    }

    /**
     * Triggered when a setup session is closed.
     * You can use this to save your sabotage related data to config in case you've been using {@link SetupSession#cacheValue(String, Object)} etc.
     */
    public abstract void onSetupSessionClose(SetupSession setupSession);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SabotageProvider) {
            return ((SabotageProvider) obj).getUniqueIdentifier().equals(getUniqueIdentifier());
        }
        return false;
    }
}
