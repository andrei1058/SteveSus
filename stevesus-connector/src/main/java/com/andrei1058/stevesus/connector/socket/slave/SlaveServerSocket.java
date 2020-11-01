package com.andrei1058.stevesus.connector.socket.slave;

import co.aikar.taskchain.TaskChain;
import com.andrei1058.stevesus.common.api.packet.PacketChannel;
import com.andrei1058.stevesus.common.api.packet.RawSocket;
import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.connector.api.arena.RemoteArena;
import com.andrei1058.stevesus.connector.api.event.SlaveConnectedEvent;
import com.andrei1058.stevesus.connector.api.event.SlaveDisconnectedEvent;
import com.andrei1058.stevesus.connector.arena.ArenaManager;
import com.andrei1058.stevesus.connector.socket.SocketManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class SlaveServerSocket implements RawSocket {

    private static final ConcurrentHashMap<String, SlaveServerSocket> sockets = new ConcurrentHashMap<>();

    private final Socket socket;
    private PrintWriter out;
    private Scanner in;
    private boolean connected = false;
    private TaskChain<?> receiveTask;
    private final String hostAndPort;
    private long lastPacket = System.currentTimeMillis();
    private String slaveBungeeName = null;

    public SlaveServerSocket(@NotNull Socket socket) {
        this.socket = socket;
        hostAndPort = socket.getInetAddress().getHostName() + ":" + socket.getPort();
        SlaveServerSocket existing = sockets.get(hostAndPort);
        if (existing != null) {
            sockets.remove(hostAndPort);
            existing.close();
        }

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException ignored) {
            out = null;
            // will try to connect later
            return;
        }

        try {
            in = new Scanner(socket.getInputStream());
        } catch (IOException ignored) {
            in = null;
            // will try to connect later
            return;
        }

        SteveSusConnector.debug("Slave server connected: " + socket.toString());

        lastPacket = System.currentTimeMillis();
        connected = true;
        sockets.put(hostAndPort, this);
        SteveSusConnector.newChain().sync(() -> Bukkit.getPluginManager().callEvent(new SlaveConnectedEvent(this))).execute();


        // listen for incoming data
        receiveTask = SteveSusConnector.newChain().async(() -> {
            while (connected) {
                if (in.hasNext()) {
                    String receivedString = in.nextLine();
                    if (receivedString != null) {
                        if (!receivedString.isEmpty()) {
                            if (receivedString.equalsIgnoreCase("disconnect")) {
                                SteveSusConnector.getInstance().getLogger().warning("Received disconnect packet. Closing socket..");
                                close();
                                return;
                            }
                            final JsonObject json;
                            try {
                                json = new JsonParser().parse(receivedString).getAsJsonObject();
                            } catch (JsonSyntaxException e) {
                                SteveSusConnector.getInstance().getLogger().warning("Received bad data from: " + hostAndPort + ". Closing socket..");
                                close();
                                return;
                            }
                            if (json != null) {
                                if (json.has("channel") && json.has("data") && json.has("server")) {
                                    String type = json.get("channel").getAsString();

                                    PacketChannel channel = SteveSusConnector.getInstance().getCommonProvider().getPacketsHandler().getChannelByName(type);
                                    if (channel != null) {
                                        if (slaveBungeeName == null) {
                                            if (json.has("server")) {
                                                String name = json.get("server").getAsString();

                                                // if name is already registered deny
                                                if (SocketManager.getSlaveByBungeeName(slaveBungeeName) != null) {
                                                    SteveSusConnector.getInstance().getLogger().severe("Denied new slave: " + getHostAndPort() + ". Reason: Bungee name already registered.");
                                                    close();
                                                    return;
                                                }
                                                slaveBungeeName = name;
                                            }
                                        }
                                        channel.read(this, json.getAsJsonObject("data"));
                                    }
                                    Instant.now();
                                    lastPacket = System.currentTimeMillis();
                                } else {
                                    SteveSusConnector.debug("Received packet (insufficient data) RS: " + receivedString);
                                    close();
                                    return;
                                }
                            } else {
                                SteveSusConnector.debug("Received packet (invalid json) RS: " + receivedString);
                                close();
                                return;
                            }
                        }
                    }
                }
            }
            close();
            Thread.currentThread().interrupt();
        });
        receiveTask.execute();
    }

    public static ConcurrentHashMap<String, SlaveServerSocket> getSockets() {
        return sockets;
    }

    public void close() {
        if (hostAndPort != null) {
            sockets.remove(hostAndPort);
            SteveSusConnector.debug("Closing socket: " + hostAndPort);
            //todo remove associated arenas
        }
        if (socket == null) return;
        connected = false;
        if (out != null) {
            out.println("disconnect");
            out.close();
        }
        if (in != null) {
            in.close();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // call disconnect event SYNC
        SteveSusConnector.newChain().sync(() -> Bukkit.getPluginManager().callEvent(new SlaveDisconnectedEvent(this))).execute();

        // drop its games
        List<RemoteArena> toRemoveList = new ArrayList<>();
        ArenaManager.getInstance().getArenas().stream().filter(arena -> arena instanceof RemoteArena)
                .filter(arena -> ((RemoteArena) arena).getServer().equals(this)).forEach(toDisable -> {
                    toRemoveList.add((RemoteArena) toDisable);
                });

        SteveSusConnector.newChain().sync(() -> toRemoveList.forEach(toRemove -> ArenaManager.getInstance().remove(toRemove))).execute();

        // this should be the last
        if (receiveTask != null) {
            receiveTask.abortChain();
        }
    }


    @Override
    public String getName() {
        return slaveBungeeName;
    }

    @Override
    public void sendPacket(JsonObject data) {
        if (out == null) return;
        if (!socket.isConnected()) {
            close();
            return;
        }
        if (out.checkError()) {
            close();
            return;
        }
        String dataPacket = data.toString();
        out.println(dataPacket);
    }

    // declare dead
    // there is a ping packet sent every 2 seconds (or configurable on slave-side)
    public boolean isTimedOut() {
        return getLastPacket() + SocketManager.TIME_OUT_TOLERANCE < System.currentTimeMillis();
    }

    public String getHostAndPort() {
        return hostAndPort;
    }

    public long getLastPacket() {
        return lastPacket;
    }
}
