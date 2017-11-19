package message;

import java.nio.ByteBuffer;
import static message.MessageConstant.*;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Interested {
    public byte[] interested = new byte[INTERESTED_LENGTH];
    private byte[] messageLength = new byte[4];
    private byte type = INTERESTED_TYPE;

    public Interested() {
        messageLength = ByteBuffer.allocate(4).putInt(0).array();

        int i = 0;
        for(; i < messageLength.length; i++) {
            interested[i] = messageLength[i];
        }
        interested[i] = type;
    }
}
