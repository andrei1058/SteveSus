package dev.andrei1058.game.common.command;

import com.andrei1058.spigot.commandlib.fast.FastRootCommand;
import com.andrei1058.spigot.commandlib.fast.FastSubCommand;
import dev.andrei1058.game.common.CommonManager;
import dev.andrei1058.game.common.api.locale.CommonLocale;
import dev.andrei1058.game.common.api.locale.CommonMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.entity.Player;

@SuppressWarnings("UnstableApiUsage")
public class LangCmd {

    private LangCmd() {
    }

    public static void register(FastRootCommand root) {
        root
                .withSubNode(
                        new FastSubCommand("lang")
                                .withAliases(new String[]{"language", "langs", "languages"})
                                .withPermAdditions(s -> s instanceof Player && !CommonManager.getINSTANCE().getCommonProvider().isInSetupSession(s))
                                .withDescription((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_LANG_DESC))
                                .withDisplayHover((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_LANG_DESC))
                                .withClickAction(ClickEvent.Action.SUGGEST_COMMAND)
                                .withExecutor((sender, args) -> {
                                    Player player = (Player) sender;
                                    CommonLocale selected = null;
                                    CommonLocale currentPlayerLang = CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getLocale(player);
                                    if (args.length == 1) {
                                        selected = CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getEnabledCommonLocales().stream().filter(lang -> lang.getIsoCode().equals(args[0]))
                                                .findFirst().orElse(null);
                                    }

                                    // if no provided option or invalid
                                    if (selected == null) {
                                        // usage
                                        player.sendMessage(currentPlayerLang.getMsg(player, CommonMessage.CMD_LANG_USAGE_HEADER).replace("{cmd}", CommonManager.getINSTANCE().getCommonProvider().getMainCommand().getName()));
                                        for (CommonLocale lang : CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getEnabledCommonLocales()) {
                                            player.sendMessage(currentPlayerLang.getMsg(player, CommonMessage.CMD_LANG_USAGE_OPTION).replace("{name}", lang.getMsg(null, CommonMessage.NAME)).replace("{code}", lang.getIsoCode()));
                                        }
                                    } else {
                                        CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().setPlayerLocale(player.getUniqueId(), selected, true);
                                        currentPlayerLang = CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getLocale(player);
                                        player.sendMessage(currentPlayerLang.getMsg(player, CommonMessage.CMD_LANG_SET).replace("{name}", currentPlayerLang.getMsg(null, CommonMessage.NAME)).replace("{code}", currentPlayerLang.getIsoCode()));
                                    }
                                })
                );
    }
}
