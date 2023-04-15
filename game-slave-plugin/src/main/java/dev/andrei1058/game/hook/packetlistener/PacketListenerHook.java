package dev.andrei1058.game.hook.packetlistener;

import dev.andrei1058.game.api.arena.Arena;
import dev.andrei1058.game.arena.ArenaManager;
import dev.andrei1058.game.common.api.arena.GameState;
import org.bukkit.Bukkit;
import org.inventivetalent.packetlistener.PacketListenerAPI;
import org.inventivetalent.packetlistener.handler.PacketHandler;
import org.inventivetalent.packetlistener.handler.ReceivedPacket;
import org.inventivetalent.packetlistener.handler.SentPacket;

public class PacketListenerHook {

    private static boolean loaded = false;

    public static void init() {
        if (loaded) return;
        if (Bukkit.getPluginManager().getPlugin("PacketListenerAPI") != null) {
            loaded = true;
            registerHeldItemListener();
        }
    }

    @SuppressWarnings("deprecation")
    private static void registerHeldItemListener() {
        PacketListenerAPI.addPacketHandler(new PacketHandler() {
            @Override
            public void onSend(SentPacket sentPacket) {
                if (sentPacket.isCancelled()) return;
                if (sentPacket.getPlayer() == null) return;
                if (sentPacket.getPacketName().equals("PacketPlayOutEntityEquipment")) {
                    Arena arena = ArenaManager.getINSTANCE().getArenaByPlayer(sentPacket.getPlayer());
                    if (arena == null) return;
                    if (arena.getGameState() != GameState.IN_GAME) return;
                    Object slot = sentPacket.getPacketValueSilent("b");
                    if (slot == null) return;
                    String slotString = slot.toString();
                    if (slotString.endsWith("HAND")) {
                        sentPacket.setCancelled(true);
                    }
                } /*else if (sentPacket.getPacketName().equals("PacketPlayOutEntityMetadata")){
                    sentPacket.getPlayer().sendMessage(sentPacket.getPacketName());
                }*/
            }

            @Override
            public void onReceive(ReceivedPacket receivedPacket) {

            }
        });
    }
}
