package message;

import java.nio.ByteBuffer;
import static message.MessageConstant.*;

/**
 * Created by qiaochu on 10/25/17.
 */
public class NotInterested {
    public byte[] notInterested = new byte[NOT_INTERESTED_LENGTH];
    private byte[] messageLength = new byte[4];
    private byte type = NOT_INTERESTED_TYPE;

    public NotInterested() {
        messageLength = ByteBuffer.allocate(4).putInt(0).array();

        int i = 0;
        for(; i < messageLength.length; i++){
            notInterested[i] = messageLength[i];
        }

        notInterested[i] = type;
    }
}
