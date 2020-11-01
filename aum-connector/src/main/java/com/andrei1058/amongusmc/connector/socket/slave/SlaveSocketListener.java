package com.andrei1058.amongusmc.connector.socket.slave;

import co.aikar.taskchain.TaskChain;
import com.andrei1058.amongusmc.connector.AmongUsConnector;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SlaveSocketListener {

    private static SlaveSocketListener INSTANCE;

    private final ServerSocket serverSocket;
    public boolean compute = true;
    private TaskChain<?> task;

    private SlaveSocketListener(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        INSTANCE = this;
        INSTANCE.task = AmongUsConnector.newChain().async(() -> {
            while (compute){
                try {
                    Socket slaveSocket = serverSocket.accept();
                    // is handled async in its class
                    new SlaveServerSocket(slaveSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        INSTANCE.task.execute();
    }

    public static void init(int port) throws IOException {
        if (INSTANCE == null){
            INSTANCE = new SlaveSocketListener(port);
        }
    }

    public static SlaveSocketListener getINSTANCE() {
        return INSTANCE;
    }

    public static void onDisable(){
        if (INSTANCE != null){
            INSTANCE.compute = false;
            if (INSTANCE.task != null) {
                try {
                    INSTANCE.task.abortChain();
                } catch (Exception ignored){
                    // idk boy. it gives null sometimes
                }
            }
            SlaveServerSocket.getSockets().forEach((key, value) -> value.close());
            Bukkit.getScheduler().cancelTasks(AmongUsConnector.getInstance());
        }
    }

}
