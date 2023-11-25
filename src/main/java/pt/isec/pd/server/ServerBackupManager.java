package pt.isec.pd.server;

import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerBackupManager extends UnicastRemoteObject implements ServerBackupInterface {

    FileOutputStream fout = null;

    public synchronized void setFout(FileOutputStream fout) {this.fout = fout;}

    public ServerBackupManager() throws RemoteException {this.fout = null;}

    @Override
    public synchronized void writeFileChunk(byte[] fileChunk, int nbytes) throws RemoteException, IOException {
        if (fout == null) {
            System.out.println(("Não existe qualquer ficheiro aberto para escrita"));
            throw new IOException("<Cli> Nao existe qualqeur ficheiro aberto para escrita");
        }
        try {
            fout.write(fileChunk, 0, nbytes);
        } catch (IOException e) {
            System.out.println("Excepçao ao escrever no ficheiro:");
        }
    }

}
