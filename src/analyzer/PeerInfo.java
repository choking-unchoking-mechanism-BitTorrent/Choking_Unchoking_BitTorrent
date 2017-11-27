package analyzer;


public class PeerInfo {
    private final String hostName;
    private final int hostID;
    private final int port;
    private final int hasCompleteFile;

    public PeerInfo(String hostName, int hostID, int port, int hasCompleteFile) {
        this.hostName = hostName;
        this.hostID = hostID;
        this.port = port;
        this.hasCompleteFile = hasCompleteFile;
    }

    public String getHostName() {
        return hostName;
    }

    public int getHostID() {
        return hostID;
    }

    public int getPort() {
        return port;
    }


    public int getHasCompleteFile() {
        return hasCompleteFile;
    }
}
