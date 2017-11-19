package message;

/**
 * Created by qiaochu on 10/25/17.
 */
import static message.MessageConstant.*;

public class Handshake {
    public byte[] handshake = new byte[HANDSHAKE_LENGTH];
    private int peerID;

    public Handshake(int peerID) {
        this.peerID = peerID;
        String temp = HANDSHAKE_HEADER + HANDSHAKE_ZERO_BITS + Integer.toString(this.peerID);
        handshake = temp.getBytes();
    }
}
