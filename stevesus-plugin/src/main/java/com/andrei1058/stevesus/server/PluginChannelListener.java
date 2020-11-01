package com.andrei1058.stevesus.server;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PluginChannelListener implements PluginMessageListener {

    // this is just for testing
    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        try {
            String subchannel = in.readUTF();
            player.sendMessage(subchannel);
            String input = in.readUTF();
            player.sendMessage(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
