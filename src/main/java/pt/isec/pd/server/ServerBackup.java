package pt.isec.pd.server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Objects;

public class ServerBackup {
    static String FILENAME = "presences.db";

    public static boolean checkDirectory(File localDirectory){

        if (!localDirectory.exists()) {
            System.out.println("A diretoria " + localDirectory + " nao existe!");
            return false;
        }
        if (!localDirectory.isDirectory()) {
            System.out.println("O caminho " + localDirectory + " nao se refere a uma diretoria!");
            return false;
        }
        if (!localDirectory.canWrite()) {
            System.out.println("Sem permissoes de escrita na diretoria " + localDirectory);
            return false;
        }
        if (Objects.requireNonNull(localDirectory.list()).length != 0) {
            System.out.println("A diretoria não está vazia.");
            return false;
        }
        return true;
    }

    public static void getDataBase(String localFilePath, ServerBackupServiceInterface remoteFileService, String objectUrl,ServerBackupManager backupServerManager){
        try (FileOutputStream localFileOutputStream = new FileOutputStream(localFilePath)) {

            System.out.println("Database created path-> " + localFilePath);

            // Gets remote service
            remoteFileService = (ServerBackupServiceInterface) Naming.lookup(objectUrl);

            // Starts local service to give access to the server
            backupServerManager = new ServerBackupManager();

            // Gives localFileOutputStream to the local service
            backupServerManager.setFout(localFileOutputStream);

            // Gets the database
            remoteFileService.getFile(FILENAME, backupServerManager);

            // Adds itself to the list on the MAIN server
            remoteFileService.addBackup(remoteFileService);


        } catch (RemoteException e) {
            System.out.println("Remote Error - " + e);
        } catch (NotBoundException e) {
            System.out.println("Unknown Remote service - " + e);
        } catch (IOException e) {
            System.out.println("E/S error - " + e);
        } catch (Exception e) {
            System.out.println("Error - " + e);
        } finally {
            if (backupServerManager != null) {

                // Ends local service to localFileOutputStream
                backupServerManager.setFout(null);

                // Ends local Service
                try {
                    UnicastRemoteObject.unexportObject(backupServerManager, true);
                } catch (NoSuchObjectException e) {
                }
            }
        }
    }

    public static void main(String[] args) throws RemoteException {

        String objectUrl;
        File localDirectory;
        String localFilePath;
        boolean flagFirstTime = true;
        int currentVersion = 0;
        int heartbeatTimeout = 30000; // 30 seconds
        long lastHeartbeatTime;

        ServerBackupManager backupServerManager = null;
        ServerBackupServiceInterface remoteFileService = null;

        if(args.length != 1){
            System.out.println("Deve passar na linha de comando: (1) a localizacao do RMI registry onte esta' ");
            return;
        }

        System.setProperty("java.rmi.server.hostname", "localhost");

        objectUrl = "rmi://localhost/servidor-backup-database";
        localDirectory = new File(args[0].trim());

        if(!checkDirectory(localDirectory))
            return;

        try {
            localFilePath = new File(localDirectory.getPath() + File.separator + FILENAME).getCanonicalPath();
        } catch (IOException ex) {
            System.out.println("Erro E/S - " + ex);
            return;
        }

        InetAddress group;
        MulticastSocket socket;
        try {
            group = InetAddress.getByName("230.44.44.44");
            socket = new MulticastSocket(4444);
            socket.joinGroup(group);
        } catch (IOException e) {
            System.out.println("Erro ao configurar o socket de multicast - " + e);
            return;
        }

        getDataBase(localFilePath, remoteFileService, objectUrl, backupServerManager);

        while (true){

            try {

                // timeout socket
                socket.setSoTimeout(heartbeatTimeout);

                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // Reset timeout
                lastHeartbeatTime = System.currentTimeMillis();

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                    HeartbeatData heartbeatData = (HeartbeatData) objectInputStream.readObject();

                    System.out.println("Received Heartbeat - Registry Port: " + heartbeatData.getRegistryPort() + ", RMI Service Name: " + heartbeatData.getRmiServiceName() + ", Current Version: " + heartbeatData.getCurrentVersion());

                    // Checks if the backup database is outdated if yes updates it, but only if its not the first time running(the first time its to get the version)
                    if (currentVersion < heartbeatData.getCurrentVersion() && !flagFirstTime) {
                        currentVersion = heartbeatData.getCurrentVersion();
                        getDataBase(localFilePath, remoteFileService, objectUrl, backupServerManager);
                    }
                    if (flagFirstTime) {
                        currentVersion = heartbeatData.getCurrentVersion();
                        flagFirstTime = false;
                    }

                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }


            }catch (SocketTimeoutException e) {
                System.out.println("Timeout: No heartbeat received within " + heartbeatTimeout + " seconds. Exiting...");
                break;
            }catch (IOException e) {
                System.out.println("Error receiving heartbeat - " + e);
            }
        }
    }
}



