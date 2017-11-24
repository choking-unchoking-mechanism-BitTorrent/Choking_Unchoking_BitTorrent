package message;

import java.nio.ByteBuffer;
import static message.MessageConstant.*;


/**
 * Created by qiaochu on 10/25/17.
 */
public class Choke extends Message{
    public byte[] choke = new byte[CHOKE_LENGTH];
    private byte[] messageLength = new byte[4];
    private byte type = CHOKE_TYPE;

    public Choke() {

        messageLength = ByteBuffer.allocate(4).putInt(0).array();

        int i = 0;
        for(; i < messageLength.length; i++) {
            choke[i] = messageLength[i];
        }
        choke[i] = type;
    }
}
