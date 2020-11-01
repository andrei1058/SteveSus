package com.andrei1058.stevesus.connector.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.BooleanProperty;
import ch.jalu.configme.properties.IntegerProperty;
import ch.jalu.configme.properties.Property;
import ch.jalu.configme.properties.StringProperty;

public class ConnectorConfig implements SettingsHolder {

    private ConnectorConfig() {
    }

    @Comment("Port used to receive data from arenas. THIS IS NOT THE MINECRAFT SERVER PORT.")
    public static final Property<Integer> LISTEN_PORT = new IntegerProperty("connector-port", 11058);
    @Comment("How many seconds before marking an arena as unreachable if no ping packets received.")
    public static final Property<Integer> TIME_OUT_TOLERANCE = new IntegerProperty("time-out-tolerance", 6);
    @Comment({" ", "Default/ fallBack server language."})
    public static final Property<String> FALLBACK_LANGUAGE = new StringProperty("default-locale", "en");
    @Comment({"Server DNS replacement for {server_name} placeholder."})
    public static final Property<String> SERVER_DISPLAY_NAME = new StringProperty("server-display-name", "mc.andrei1058.com");
    @Comment("Languages folder path.")
    public static final Property<String> LANGUAGES_FOLDER = new StringProperty("custom-path.locales-path", "");
    @Comment("Database folder path. Where's database.yml located?")
    public static final Property<String> DATABASE_PATH = new StringProperty("custom-path.database-path", "");
    @Comment("Directory where to get selector.yml from.")
    public static final Property<String> SELECTOR_PATH = new StringProperty("custom-path.selector-path", "");
    @Comment("Stats GUIs configuration path.")
    public static final Property<String> STATS_PATH = new StringProperty("custom-path.stats-path", "");

    @Comment("Allow parties?")
    public static final Property<Boolean> PARTIES_ENABLE = new BooleanProperty("parties.enable", true);
    @Comment("Should '/x party' sub-command be registered?")
    public static final Property<Boolean> PARTIES_ENABLE_COMMAND = new BooleanProperty("parties.internal.register-sub-command", true);
    @Comment("How many seconds before removing member from a party if he disconnects? -1/ 0 for always")
    public static final Property<Integer> PARTIES_DISBAND_AFTER = new IntegerProperty("parties.internal.offline-tolerance", 60 * 60);
    @Comment("How many players are allowed in a party?")
    public static final Property<Integer> PARTIES_SIZE_LIMIT = new IntegerProperty("parties.internal.party-size-limit", 10);


    @Override
    public void registerComments(CommentsConfiguration conf) {
        conf.setComment("",
                "Plugin made with love and M0nst3r Energy in Romania by andrei1058",
                "Website: https://andrei1058.com",
                "Discord: https://discord.gg/XdJfN2X", " ", " ");

        // custom paths
        conf.setComment("custom-path", " ", "Useful if you want to make your servers use the same files.", "This is compatible as well with the main plugin. Leave empty for default.");

        // parties
        conf.setComment("parties", " ", "Parties configuration.");
        conf.setComment("parties.internal", "Default parties implementation configuration.");
    }
}
