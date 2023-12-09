package pt.isec.pd.server;

import pt.isec.pd.server.databaseManagement.DatabaseVersionControlManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class HeartbeatSender implements Runnable {
    public InetAddress group;
    public DatagramSocket socket;
    DatabaseVersionControlManager vs;
    public int currentVersion;
    private int registryPort;
    private String rmiServiceName;

    // Constructor for the Server Thread that receivs the DatabaseVersionControlManager to send the current version every 5sec
    public HeartbeatSender(DatabaseVersionControlManager vs, int registryPort, String rmiServiceName) {
        this.registryPort = registryPort;
        this.rmiServiceName = rmiServiceName;
        this.vs = vs;
        try {
            this.group = InetAddress.getByName("230.44.44.44");
            this.socket = new DatagramSocket();
        } catch (IOException e) {
            System.out.println("Error in heartbeat");
            e.printStackTrace();
        }
    }

    // Constructor for the DatabaseVersionControlManager, so it can send a heartbeat with the current version every time is changed
    public HeartbeatSender() {
        try {
            this.group = InetAddress.getByName("230.44.44.44");
            this.socket = new DatagramSocket();
        } catch (IOException e) {
            System.out.println("Error in heartbeat");
            e.printStackTrace();
        }
    }

    public void sendHeartbeat(int currentVersion) {
        this.currentVersion = currentVersion;
        try {
            HeartbeatData heartbeatData = new HeartbeatData(registryPort, rmiServiceName, currentVersion);
            byte[] data = heartbeatData.dataByte();
            DatagramPacket packet = new DatagramPacket(data, data.length, group, 4444);
            socket.send(packet);

       //     System.out.println("Heartbeat from server - " + currentVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                sendHeartbeat(vs.getCurrentVersion());
                Thread.sleep(10000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
