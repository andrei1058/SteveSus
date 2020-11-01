package com.andrei1058.amongusmc.connector.socket.task;

import com.andrei1058.amongusmc.connector.AmongUsConnector;
import com.andrei1058.amongusmc.connector.socket.slave.SlaveServerSocket;

public class TimeOutCheckerTask {

    private static boolean initialized = false;

    private TimeOutCheckerTask(){}

    public static void init(){
        if (initialized) return;
        initialized = true;
        AmongUsConnector.newChain().async(()-> {
            SlaveServerSocket.getSockets().values().forEach(slave -> {
                if (slave.isTimedOut()){
                    AmongUsConnector.debug("Slave timed out: " + slave.getHostAndPort());
                    slave.close();
                }
            });
        }).execute();
    }
}
