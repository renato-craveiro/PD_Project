package pt.isec.pd.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerBackupServiceInterface extends Remote {

    public byte [] getFileChunk(String dbName, long offset) throws RemoteException, java.io.IOException;

    void getFile(String dbName, ServerBackupInterface servBackupRef) throws RemoteException, java.io.IOException;

    void addBackup(ServerBackupServiceInterface sBackup) throws RemoteException;

    void removeBackup(ServerBackupServiceInterface sBackup) throws RemoteException;

}
