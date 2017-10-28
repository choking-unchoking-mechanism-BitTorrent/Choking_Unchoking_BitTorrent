package message;

import java.nio.ByteBuffer;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Request {
    public byte[] request = new byte[9];
    private byte[] messageLength = new byte[4];
    private final static byte type = 6;
    private byte[] payload = new byte[4];

    public Request(int pieceIndex) {
        messageLength = ByteBuffer.allocate(4).putInt(4).array();
        payload = ByteBuffer.allocate(4).putInt(pieceIndex).array();

        int i = 0;
        for(; i < messageLength.length; i++) {
            request[i] = messageLength[i];
        }
        request[i] = type;

        for(int j = 0; j < payload.length; j++) {
            i++;
            request[i] = payload[j];
        }
    }
}
