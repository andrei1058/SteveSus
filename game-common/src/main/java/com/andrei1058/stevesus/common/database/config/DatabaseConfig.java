package com.andrei1058.stevesus.common.database.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.*;

public class DatabaseConfig implements SettingsHolder {

    @Comment("Set this to true if you want to store player data on a database.")
    public static final Property<Boolean> DATABASE_ENABLED = new BooleanProperty("enabled", false);
    @Comment("Database host address.")
    public static final Property<String> DATABASE_HOST = new StringProperty("host", "localhost");
    @Comment("Database host address.")
    public static final Property<Integer> DATABASE_PORT = new IntegerProperty("port", 3306);
    @Comment("Database SSL/ TSL encryption.")
    public static final Property<Boolean> DATABASE_SSL = new BooleanProperty("ssl", false);
    @Comment("Database name.")
    public static final Property<String> DATABASE_NAME = new StringProperty("name", "myDatabase");
    @Comment("Database user.")
    public static final Property<String> DATABASE_USER = new StringProperty("user", "notRoot");
    @Comment("Database user password.")
    public static final Property<String> DATABASE_PASS = new StringProperty("password", "somethingIntelligent");
    @Comment("Verify server certificate.")
    public static final OptionalProperty<Boolean> DATABASE_VERIFY_CERTIFICATE = new OptionalProperty<>(new BooleanProperty("verify-certificate", true));
    @Comment("Database pool size.")
    public static final OptionalProperty<Integer> DATABASE_POOL_SIZE = new OptionalProperty<>(new IntegerProperty("pool-size", 10));
    @Comment("Max lifetime in seconds.")
    public static final OptionalProperty<Integer> DATABASE_MAX_LIFETIME = new OptionalProperty<>(new IntegerProperty("max-lifetime", 1800));

    @Override
    public void registerComments(CommentsConfiguration conf) {
        conf.setComment("",
                "Plugin made with love and M0nst3r Energy in Romania by andrei1058",
                "Website: https://andrei1058.com",
                "Discord: https://discord.gg/XdJfN2X", " ", "Database information is not a must.");
    }
}
