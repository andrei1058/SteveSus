package com.andrei1058.stevesus.connector.socket.task;

import com.andrei1058.stevesus.connector.SteveSusConnector;
import com.andrei1058.stevesus.connector.socket.slave.SlaveServerSocket;

public class TimeOutCheckerTask {

    private static boolean initialized = false;

    private TimeOutCheckerTask(){}

    public static void init(){
        if (initialized) return;
        initialized = true;
        SteveSusConnector.newChain().async(()-> {
            SlaveServerSocket.getSockets().values().forEach(slave -> {
                if (slave.isTimedOut()){
                    SteveSusConnector.debug("Slave timed out: " + slave.getHostAndPort());
                    slave.close();
                }
            });
        }).execute();
    }
}
