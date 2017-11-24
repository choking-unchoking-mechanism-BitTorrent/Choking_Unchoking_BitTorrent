package message;

import java.nio.ByteBuffer;
import static message.MessageConstant.*;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Unchoke {
    public byte[] unchoke = new byte[UNCHOKE_LENGTH];
    private byte[] messageLength = new byte[4];
    private final static byte type = 1;

    public Unchoke() {
        messageLength = ByteBuffer.allocate(4).putInt(0).array();

        int i = 0;
        for(; i < messageLength.length; i++) {
            unchoke[i] = messageLength[i];
        }

        unchoke[i] = type;
    }
}
