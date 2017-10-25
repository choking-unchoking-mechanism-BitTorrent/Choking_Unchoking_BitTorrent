package message;

import java.nio.ByteBuffer;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Choke {
    public byte[] choke = new byte[5];
    private byte[] messageLength = new byte[4];
    private byte type = 0;

    public Choke() {
        messageLength = ByteBuffer.allocate(4).putInt(0).array();

        int i = 0;
        for(; i < messageLength.length; i++) {
            choke[i] = messageLength[i];
        }
        choke[i] = type;
    }
}
