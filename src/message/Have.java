package message;

import java.nio.ByteBuffer;
import static message.MessageConstant.*;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Have {

    public byte[] have = new byte[HAVE_LENGTH];
    private byte[] messageLength = new byte[4];
    private byte type = HAVE_TYPE;
    private byte[] payload = new byte[4];

    public Have(int index) {
        messageLength = ByteBuffer.allocate(4).putInt(4).array();
        payload = ByteBuffer.allocate(4).putInt(index).array();

        int i = 0;
        for(; i < messageLength.length; i++){
            have[i] = messageLength[i];
        }
        have[i] = type;

        for(int j = 0; j < payload.length; j++) {
            i++;
            have[i] = payload[j];
        }
    }

}
