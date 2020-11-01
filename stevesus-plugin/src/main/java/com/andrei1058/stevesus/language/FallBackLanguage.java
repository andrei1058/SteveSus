package com.andrei1058.stevesus.language;

import com.andrei1058.stevesus.api.locale.Message;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;

public class FallBackLanguage extends Language {

    protected FallBackLanguage() {
        super("en");
    }

    public static void initDefaultMessages(Language lang) {
        lang.getYml().options().copyDefaults(true);
        YamlConfiguration yml = lang.getYml();

        if (lang instanceof FallBackLanguage) {
            yml.addDefault(CommonMessage.NAME.toString(), "English");
            lang.save();
        }

        // save default messages that do not require manual implementations
        CommonMessage.saveDefaults(yml);

        CommonMessage.SELECTOR_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "-"}, "&2Fast Join");
        CommonMessage.SELECTOR_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "-"}, Arrays.asList(" ", "&fJoin an arena that", "&fis about to start."));
        CommonMessage.SELECTOR_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "z"}, "&6Spectate a Game");
        CommonMessage.SELECTOR_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "z"}, Arrays.asList(" ", "&fChoose a started", "&fgame to spectate."));


        lang.getYml().addDefault(CommonMessage.ARENA_SELECTOR_GUI_NAME.toString() + "-spectate", "&8Spectate a Game");
        CommonMessage.SELECTOR_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "spectate", "{r}", "4"}, "&2Play");
        CommonMessage.SELECTOR_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "spectate", "{r}", "4"}, Arrays.asList(" ", "&fSelect a game", "&fto play in."));


        CommonMessage.STATS_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "*"}, "&eFirst Game");
        CommonMessage.STATS_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "*"}, Arrays.asList(" ", "&f{first_play}"));
        CommonMessage.STATS_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "-"}, "&eLast Game");
        CommonMessage.STATS_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "-"}, Arrays.asList(" ", "&f{last_play}"));

        lang.save();

        // save default messages that do not require manual implementations
        Message.saveDefaults(yml);

        Message.TELEPORTER_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "*"}, "&b&l{target}");
        Message.TELEPORTER_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "*"}, Arrays.asList(" ", "&fClick to teleport", "&fto this player!"));

        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "50", "&b50");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "40", "&b40");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "30", "&b30");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "20", "&b20");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "10", "&b10");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "5", "&a5");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "4", "&a4");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "3", "&a3");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "2", "&a2");
        lang.getYml().addDefault(Message.COUNT_DOWN_TITLE_PATH.toString() + "1", "&a1");

        lang.getYml().options().header("If you want to disable this language set enable: false own bellow. This option is ignored for the default server language");
        lang.getYml().options().copyDefaults(true);
        lang.save();
    }
}
