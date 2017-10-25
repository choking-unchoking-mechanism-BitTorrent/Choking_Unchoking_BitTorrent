package message;

import java.nio.ByteBuffer;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Unchoke {
    public byte[] unchoke = new byte[5];
    private byte[] messageLength = new byte[4];
    private byte type = 1;

    public Unchoke() {
        messageLength = ByteBuffer.allocate(4).putInt(0).array();

        int i = 0;
        for(; i < messageLength.length; i++) {
            unchoke[i] = messageLength[i];
        }

        unchoke[i] = type;
    }
}
