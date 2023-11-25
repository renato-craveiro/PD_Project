package pt.isec.pd.server;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.Naming;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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
        return true;
    }

    public static void getDataBase(String localFilePath, ServerBackupServiceInterface remoteFileService, String objectUrl,ServerBackupManager backupServerManager){
        try (FileOutputStream localFileOutputStream = new FileOutputStream(localFilePath)) { //Cria o ficheiro local

            System.out.println("Ficheiro " + localFilePath + " criado.");

            // Obtem a referencia remota para o servico com nome "servidor-ficheiros-pd".
            remoteFileService = (ServerBackupServiceInterface) Naming.lookup(objectUrl);/*...*/

            // Lanca o servico local para acesso remoto por parte do servidor.
            backupServerManager = new ServerBackupManager(); /*...*/

            // Passa ao servico RMI LOCAL uma referencia para o objecto localFileOutputStream.
            backupServerManager.setFout(localFileOutputStream);/*...*/

            // Obtem o ficheiro pretendido, invocando o metodo getFile no servico remoto.
            remoteFileService.getFile(FILENAME, backupServerManager);

            // Regista-se na lista de Servers
            remoteFileService.addBackup(remoteFileService);



        } catch (RemoteException e) {
            System.out.println("Erro remoto - " + e);
        } catch (NotBoundException e) {
            System.out.println("Servico remoto desconhecido - " + e);
        } catch (IOException e) {
            System.out.println("Erro E/S - " + e);
        } catch (Exception e) {
            System.out.println("Erro - " + e);
        }/* finally {
            if (backupServerManager != null) {

                // Retira do servico local a referencia para o objecto localFileOutputStream.
                backupServerManager.setFout(null);

                // Termina o serviço local.
                try {
                    UnicastRemoteObject.unexportObject(backupServerManager, true);
                } catch (NoSuchObjectException e) {
                }
            }
        }*/
    }

    public static void main(String[] args) {

        String objectUrl;
        File localDirectory;
        String localFilePath;
        boolean flagFirstTime = true;
        int currentVersion = 0;

        ServerBackupManager backupServerManager = null;
        ServerBackupServiceInterface remoteFileService = null;


        if(args.length != 1){
            System.out.println("Deve passar na linha de comando: (1) a localizacao do RMI registry onte esta' ");
            return;
        }

        System.setProperty("java.rmi.server.hostname", "192.168.1.100"); //colocar o nosso endereco ip da placa de rede sem fios

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

        // Criação do grupo de multicast e socket
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
                // Configuração do pacote para receber o heartbeat
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
                try (ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
                    HeartbeatData heartbeatData = (HeartbeatData) objectInputStream.readObject();

                    System.out.println("Received Heartbeat - RMI Registry Port: " + heartbeatData.getRmiRegistryPort() + ", RMI Service Name: " + heartbeatData.getRmiServiceName() + ", Current Version: " + heartbeatData.getCurrentVersion());

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



            } catch (IOException e) {
                System.out.println("Erro ao receber o heartbeat - " + e);
            }
        }


    }
}



