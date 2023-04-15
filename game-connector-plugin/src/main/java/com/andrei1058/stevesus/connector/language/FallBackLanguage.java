package com.andrei1058.stevesus.connector.language;

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


        lang.addDefault(CommonMessage.ARENA_SELECTOR_GUI_NAME + "-spectate", "&8Spectate a Game");
        CommonMessage.SELECTOR_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "spectate", "{r}", "4"}, "&2Play");
        CommonMessage.SELECTOR_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "spectate", "{r}", "4"}, Arrays.asList(" ", "&fSelect a game", "&fto play in."));

        CommonMessage.STATS_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "*"}, "&eFirst Game");
        CommonMessage.STATS_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "*"}, Arrays.asList(" ", "&f{first_play}"));
        CommonMessage.STATS_REPLACEMENT_ITEM_NAME_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "-"}, "&eLast Game");
        CommonMessage.STATS_REPLACEMENT_ITEM_LORE_PATH.addDefault(yml, new String[]{"{s}", "main", "{r}", "-"}, Arrays.asList(" ", "&f{last_play}"));

        lang.getYml().options().header("If you want to disable this language set enable: false down bellow. \n!This option is ignored for the default server language!");
        lang.getYml().options().copyDefaults(true);
        lang.save();
    }
}
