package message;

import java.nio.ByteBuffer;
import static message.MessageConstant.*;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Request {
    public byte[] request = new byte[REQUEST_LENGTH];
    private byte[] messageLength = new byte[4];
    private byte type = REQUEST_TYPE;
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
