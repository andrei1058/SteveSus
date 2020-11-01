package com.andrei1058.stevesus.server.bungee.remote;

import co.aikar.taskchain.TaskChain;
import com.andrei1058.stevesus.SteveSus;
import com.andrei1058.stevesus.arena.ArenaHandler;
import com.andrei1058.stevesus.common.CommonManager;
import com.andrei1058.stevesus.common.api.packet.DefaultChannel;
import com.andrei1058.stevesus.common.api.packet.PacketChannel;
import com.andrei1058.stevesus.common.api.packet.RawSocket;
import com.andrei1058.stevesus.config.MainConfig;
import com.andrei1058.stevesus.server.ServerManager;
import com.andrei1058.stevesus.server.bungee.packet.FullDataArenaPacket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteLobby implements RawSocket {

    private static int TIME_OUT_TOLERANCE = 0;

    private static final ConcurrentHashMap<String, RemoteLobby> sockets = new ConcurrentHashMap<>();

    private Socket socket;
    private PrintWriter out;
    private Scanner in;
    private String host;
    private int port;
    private boolean connected = false;
    private TaskChain<?> receiveTask;
    private String hostAndPort;
    private long lastPacket;

    public RemoteLobby(String hostAndPort) {
        if (TIME_OUT_TOLERANCE == 0) {
            TIME_OUT_TOLERANCE = ServerManager.getINSTANCE().getConfig().getProperty(MainConfig.BUNGEE_AUTO_SCALE_PING_LOBBIES_INTERVAL) * 3000;
        }

        RemoteLobby existing = sockets.get(hostAndPort);
        if (existing != null) {
            sockets.remove(hostAndPort);
            existing.close();
        }

        String[] address = hostAndPort.split(":");
        if (address.length != 2) {
            SteveSus.getInstance().getLogger().warning("Bad RemoteLobby address: " + hostAndPort);
            return;
        }

        try {
            port = Integer.parseInt(address[1]);
        } catch (Exception ex) {
            SteveSus.getInstance().getLogger().warning("Bad RemoteLobby address: " + hostAndPort);
            return;
        }

        host = address[0];
        this.hostAndPort = hostAndPort;
        sockets.put(hostAndPort, this);
        connect();
    }

    public void connect() {
        try {
            socket = new Socket(host, port);
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
        } catch (IOException ignored) {
            // will try to connect later
            return;
        }
        lastPacket = System.currentTimeMillis();
        connected = true;
        SteveSus.debug("Connected new remote lobby: " + hostAndPort);

        // send existing arenas
        if (ArenaHandler.getINSTANCE() != null) {
            ArenaHandler.getINSTANCE().getArenas().forEach(arena -> CommonManager.getINSTANCE().getCommonProvider().getPacketsHandler().sendPacket(this, DefaultChannel.ARENA_FULL_DATA.toString(), new FullDataArenaPacket(arena), false));
        }

        // listen for incoming data
        receiveTask = SteveSus.newChain().async(() -> {
            while (connected) {
                if (in.hasNext()) {
                    String receivedString = in.nextLine();
                    if (receivedString != null) {
                        if (!receivedString.isEmpty()) {
                            if (receivedString.equalsIgnoreCase("disconnect")) {
                                close();
                                return;
                            }
                            final JsonObject json;
                            try {
                                json = new JsonParser().parse(receivedString).getAsJsonObject();
                            } catch (JsonSyntaxException e) {
                                SteveSus.getInstance().getLogger().warning("Received bad data from: " + socket.getInetAddress().toString() + ". Closing socket..");
                                close();
                                return;
                            }
                            if (json != null) {
                                if (json.has("channel") && json.has("data")) {
                                    // 0 for player joining an arena
                                    String type = json.get("channel").getAsString();

                                    PacketChannel channel = SteveSus.getInstance().getCommonProvider().getPacketsHandler().getChannelByName(type);
                                    if (channel != null) {
                                        channel.read(this, json.getAsJsonObject("data"));
                                    }
                                    lastPacket = System.currentTimeMillis();
                                } else {
                                    close();
                                    return;
                                }
                            } else {
                                close();
                                return;
                            }
                        }
                    }
                }
            }
            close();
        });
        receiveTask.execute();
    }

    public boolean isConnected() {
        return connected;
    }

    // declare dead
    // there is a ping packet sent every 2 seconds (or configurable on slave-side)
    public boolean isTimedOut() {
        return getLastPacket() + TIME_OUT_TOLERANCE < System.currentTimeMillis();
    }

    public void close() {
        if (!connected) return;
        long time = System.currentTimeMillis();
        if (hostAndPort != null) {
            //sockets.remove(hostAndPort);
            SteveSus.debug("Closing socket: " + hostAndPort);
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
        if (receiveTask != null) {
            try {
                receiveTask.abortChain();
            } catch (Exception ignored) {
                // idk man, it gives npe
            }
        }
        socket = null;
        out = null;
        in = null;
        long closeTime = System.currentTimeMillis() - time;
        SteveSus.debug("Took " + closeTime + "ms to close slave socket: " + getName());
    }

    @Override
    public String getName() {
        return hostAndPort;
    }

    public void sendPacket(JsonObject data) {
        if (!isConnected()) return;
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

    @Override
    public long getLastPacket() {
        return lastPacket;
    }

    public static ConcurrentHashMap<String, RemoteLobby> getSockets() {
        return sockets;
    }
}
