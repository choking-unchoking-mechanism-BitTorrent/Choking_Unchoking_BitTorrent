package message;

/**
 * Created by qiaochu on 10/25/17.
 */
import java.nio.ByteBuffer;
import exception.message.HandshakeMessageException;

import static message.MessageConstant.*;

public class Handshake {
    public byte[] handshake = new byte[HANDSHAKE_LENGTH];
    private int peerID;

    public Handshake(int peerID) {
        this.peerID = peerID;
        String temp = HANDSHAKE_HEADER + HANDSHAKE_ZERO_BITS;
        byte[] tempArray = temp.getBytes();
        for (int i = 0; i < temp.length(); i++) {
            handshake[i] = tempArray[i];
        }
        int next = temp.length();
        byte[] peerIDArray = ByteBuffer.allocate(4).putInt(peerID).array();
        for (int i = 0; i < 4; i++){
            handshake[i + next] = peerIDArray[i];
        }
    }
    public Handshake(byte[] message) throws HandshakeMessageException{
        if (message.length != MessageConstant.HANDSHAKE_LENGTH) {
            throw new HandshakeMessageException();
        }
        for (int i = 0; i < HANDSHAKE_LENGTH; i++){
            handshake[i] = message[i];
        }
        byte[] peerIdArray = new byte[4];
        for (int i = 0; i < 4; i++){
              peerIdArray[i] = handshake[HANDSHAKE_LENGTH - 4 + i];
        }
        peerID = java.nio.ByteBuffer.wrap(peerIdArray).getInt();
    }

    public int getPeerID() {
        return peerID;
    }

    public byte[] getHandshake() {
        return handshake;
    }
}
