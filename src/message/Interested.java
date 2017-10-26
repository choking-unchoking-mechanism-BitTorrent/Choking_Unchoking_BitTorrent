package message;

import java.nio.ByteBuffer;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Interested {
    public byte[] interested = new byte[5];
    private byte[] messageLength = new byte[4];
    private final static byte type = 2;

    public Interested() {
        messageLength = ByteBuffer.allocate(4).putInt(0).array();

        int i = 0;
        for(; i < messageLength.length; i++) {
            interested[i] = messageLength[i];
        }
        interested[i] = type;
    }
}
