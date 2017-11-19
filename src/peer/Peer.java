package peer;

import analyzer.PeerInfo;
import message.BitField;

public class Peer {
    private String host;
    private int port;
    private int peerId;
    private BitField bitField;
    private Boolean isChocked;

    public Peer(PeerInfo peerInfo, int piecesNumber) {
        this.host = peerInfo.getHostName();
        this.port = peerInfo.getPort();
        this.peerId = peerInfo.getPeerId();
        if (peerInfo.getHasCompleteFile() == 1)
            this.bitField.setBitfield(true, piecesNumber);
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
}
