package message;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Handshake {
    public byte[] handshake = new byte[32];
    private String header = "P2PFILESHARINGPROJ";
    private String zeroBits = "0000000000";
    private int peerID;

    public Handshake(int peerID) {
        this.peerID = peerID;
        String temp = header + zeroBits + Integer.toString(this.peerID);
        handshake = temp.getBytes();
    }
}
