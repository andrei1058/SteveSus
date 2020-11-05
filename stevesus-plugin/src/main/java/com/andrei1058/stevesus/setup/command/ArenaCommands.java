package com.andrei1058.stevesus.setup.command;

import ch.jalu.configme.SettingsManager;
import ch.jalu.configme.properties.OptionalProperty;
import ch.jalu.configme.properties.Property;
import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import com.andrei1058.stevesus.api.setup.SetupSession;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.config.ArenaConfig;
import com.andrei1058.stevesus.setup.SetupManager;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class ArenaCommands {

    private ArenaCommands() {
    }

    public static void register(FastRootCommand root) {

        FastSubCommand toggleLoadAtStartup = new FastSubCommand("toggleLoadAtStartup");
        FastSubCommand saveChanges = new FastSubCommand("saveChanges");

        root.withSubNode(toggleLoadAtStartup
                .withClickAction(ClickEvent.Action.RUN_COMMAND)
                .withPermAdditions(s -> SetupManager.getINSTANCE().isInSetup(s))
                .withDisplayName(s -> "&7" + toggleLoadAtStartup.getName() + " ")
                .withDisplayHover(s -> "&a&oOptional\n " +
                        "\n&fToggle whether to create or not games from this" +
                        "\n&ftemplate when the server starts.\n" +
                        " " +
                        "\n&fYou may need to set this to false if this" +
                        "\n&ftemplate is going to be handled by an addon." +
                        "\n&eCurrent value: &b" + getCurrentProperty(ArenaConfig.LOAD_AT_START_UP, s) +
                        "\n&eDefault value: " + ArenaConfig.LOAD_AT_START_UP.getDefaultValue())
                .withExecutor((s, args) -> {
                    Player player = (Player) s;
                    SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(player.getWorld().getName(), true);
                    boolean current = config.getProperty(ArenaConfig.LOAD_AT_START_UP);
                    config.setProperty(ArenaConfig.LOAD_AT_START_UP, current = !current);
                    config.save();
                    player.sendMessage(ChatColor.GRAY + "Creating games from this template at server start-up is now set to: " + ChatColor.AQUA + current);
                }));

        // set sub command
        SetCommand.append(root);
        // add sub command
        AddCommand.append(root);
        // remove sub command
        RemoveCommand.append(root);

        root.withSubNode(saveChanges
                .withAliases(new String[]{"done", "finish", "close", "save"})
                .withPermAdditions((s) -> SetupManager.getINSTANCE().isInSetup(s))
                .withDisplayName(s -> "&7" + saveChanges.getName())
                .withDisplayHover((s) -> "&fClose setup session ans save changes.")
                .withExecutor((s, args) -> {
                    SetupSession setupSession = SetupManager.getINSTANCE().getSession((Player) s);
                    SetupManager.getINSTANCE().removeSession(setupSession);
                    //noinspection ConstantConditions
                    s.sendMessage(ChatColor.GRAY + "Changes saved for " + ChatColor.GREEN + setupSession.getWorldName() + ChatColor.GRAY + ".");
                }))

        ;
    }

    protected static <T> T getCurrentProperty(Property<T> property, CommandSender player) {
        if (!(player instanceof Player)) return property.getDefaultValue();
        SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(((Player) player).getWorld().getName(), true);
        return config.getProperty(property);
    }

    protected static boolean isSet(OptionalProperty<?> property, CommandSender player) {
        SettingsManager config = ArenaHandler.getINSTANCE().getTemplate(((Player) player).getWorld().getName(), true);
        return config.getProperty(property).isPresent();
    }
}
