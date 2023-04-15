package dev.andrei1058.game.connector.socket.task;

import dev.andrei1058.game.connector.SteveSusConnector;
import dev.andrei1058.game.connector.socket.slave.SlaveServerSocket;

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
