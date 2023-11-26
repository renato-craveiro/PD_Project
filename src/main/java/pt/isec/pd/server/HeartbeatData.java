package pt.isec.pd.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class HeartbeatData implements Serializable {
    private int registryPort;
    private String rmiServiceName;
    private int currentVersion;

    public HeartbeatData(int registryPort, String rmiServiceName, int currentVersion) {
        this.registryPort = registryPort;
        this.rmiServiceName = rmiServiceName;
        this.currentVersion = currentVersion;
    }
    public int getRegistryPort() {
        return registryPort;
    }

    public String getRmiServiceName() {
        return rmiServiceName;
    }

    public int getCurrentVersion() {
        return currentVersion;
    }

    public byte[] dataByte() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(this);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
