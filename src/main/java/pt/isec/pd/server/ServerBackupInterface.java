package pt.isec.pd.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerBackupInterface extends Remote {

    void writeFileChunk(byte [] fileChunk, int nbytes) throws RemoteException, java.io.IOException;

}
