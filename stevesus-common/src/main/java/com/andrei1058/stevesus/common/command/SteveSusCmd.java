package com.andrei1058.stevesus.common.command;

import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.locale.CommonMessage;
import com.andrei1058.spigot.commandlib.FastRootCommand;

import java.util.Arrays;

public class SteveSusCmd extends FastRootCommand {

    protected SteveSusCmd(String name) {
        super(name);
        withAliases(new String[]{"ss", "stevesus", "amongus", "sus"});
        withHeaderContent("&1|| &3" + CommonManager.getINSTANCE().getPlugin().getName() + "&7 by " + Arrays.toString(CommonManager.getINSTANCE().getPlugin().getDescription().getAuthors().toArray()))
                .withHeaderHover("&av" + CommonManager.getINSTANCE().getPlugin().getDescription().getVersion()).withDeniedMsg((s) -> CommonManager.getINSTANCE().getCommonProvider().getCommonLocaleManager().getMsg(s, CommonMessage.CMD_PERMISSION_DENIED))
                .withDisplayName((s) -> "&3/&f" + getName() + " ");
        //todo add git version and on click open plugin link

    }
}
