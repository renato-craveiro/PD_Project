package pt.isec.pd.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerBackupServiceInterface extends Remote {

    public byte [] getFileChunk(String fileName, long offset) throws RemoteException, java.io.IOException;

    void getFile(String fileName, ServerBackupInterface cliRef) throws java.rmi.RemoteException, java.io.IOException;

    void addBackup(ServerBackupServiceInterface observer) throws RemoteException;

    void removeBackup(ServerBackupServiceInterface observer) throws RemoteException;

}
