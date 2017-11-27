package peer;

import analyzer.PeerInfo;
import message.BitField;

public class Peer {
    private String host;
    private int port;
    private int peerId;
    private BitField bitField;
    private Boolean isChocked;
    private boolean hasCompleteFile;

    public Peer(PeerInfo peerInfo, int piecesNumber) {
        this.host = peerInfo.getHostName();
        this.port = peerInfo.getPort();
        this.peerId = peerInfo.getHostID();
        this.hasCompleteFile = peerInfo.getHasCompleteFile() == 1;
        this.bitField = new BitField();
        if (peerInfo.getHasCompleteFile() == 1)
            this.bitField.setBitField(true, piecesNumber);
        else
            this.bitField.setBitField(false, piecesNumber);
    }
    public boolean getHasCompleteFile(){
        return hasCompleteFile;
    }
    public void setHasCompleteFile(boolean hasCompleteFile){
        this.hasCompleteFile = hasCompleteFile;
    }
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPeerId() {
        return peerId;
    }

    public void setPeerId(int peerId) {
        this.peerId = peerId;
    }

    public BitField getBitField() {
        return bitField;
    }

    public void setBitField(BitField bitField) {
        this.bitField = bitField;
    }

    public Boolean getChocked() {
        return isChocked;
    }

    public void setChocked(Boolean chocked) {
        isChocked = chocked;
    }
}
