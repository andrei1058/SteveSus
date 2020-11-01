package com.andrei1058.stevesus.prevention.config;

import ch.jalu.configme.Comment;
import ch.jalu.configme.SettingsHolder;
import ch.jalu.configme.configurationdata.CommentsConfiguration;
import ch.jalu.configme.properties.*;
import com.andrei1058.stevesus.api.prevention.abandon.TriggerType;

public class AbusePreventionConfig implements SettingsHolder {

    @Comment({"Set this to false if you want to disable the abandon system."})
    public static final Property<Boolean> ABANDON_SYSTEM_ENABLE = new BooleanProperty("abandon-system.enable", true);

    @Comment({"Which way to detect quitting players. Available types:",
            "ARENA_LEAVE which means every arena leave will be checked.",
            "COMMAND which will trigger this system only if a player quits by some specified commands."})
    public static final Property<TriggerType> ABANDON_SYSTEM_TRIGGER = new EnumProperty<>(TriggerType.class, "abandon-system.trigger", TriggerType.ARENA_LEAVE);

    @Comment("Commands used if you set the option above to COMMAND.")
    public static final Property<String> ABANDON_COMMANDS = new StringProperty("abandon-system.command-triggers", "hub, leave, lobby");

    @Comment("Conditions to be satisfied in order to declare a game abandon.")
    public static final Property<String> CONDITIONS = new StringProperty("abandon-system.conditions", "played_less_than:300");

    @Comment({"Should it be enabled?"})
    public static final Property<Boolean> ANTI_FARMING_ENABLE = new BooleanProperty("anti-farming-system.enable", true);

    @Comment({"A player must play longer than this value in order to gain stats.", "This part is ignored if a player leaves and it results as an abandon.", "Instead, this is useful when there are 2 friends for example killing each other and", "try to farm wins etc making games last a few minutes."})
    public static final Property<Integer> ANTI_FARMING_MIN_MATCH_TIME = new IntegerProperty("anti-farming-system.min-play-time", 240);

    @Override
    public void registerComments(CommentsConfiguration conf) {
        // header
        conf.setComment("", "Plugin by andrei1058.", " ", " ");

        conf.setComment("abandon-system", "The abandon system is used to improve the end-user experience.",
                "A simple example is that players abandoning the game will not get stats.", "Note that eliminated players are skipped from this checks.",
                "It is implicit as well that this system will run on IN_GAME state only.");

        conf.setComment("anti-farming-system", " ", "Anti stats farming options.");
    }
}
