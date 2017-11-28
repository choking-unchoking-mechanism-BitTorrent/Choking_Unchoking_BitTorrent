package message;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by drinkmowater on 11/20/17.
 */
public class Message {
    private int length;
    private byte type;
    private byte[] payload;

    public Message() {
    }

    public Message(int length, byte type, byte[] payload) {

        this.length = length - 4;
        this.type = type;
        this.payload = payload;
    }

    public int getLength() {
        return length;
    }

    public byte getType() {
        return type;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setLength(int length) {
        this.length = length - 4;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
    public byte[] getMessageByteArray(){
        byte[] result;
        if (payload == null)
            result = new byte[5];
        else
            result = new byte[5 + payload.length];
        byte[] lengthArray = ByteBuffer.allocate(4).putInt(length).array();
        //Combine three contents into result
        for (int i = 0; i < 4; i++){
            result[i] = lengthArray[i];
        }
        result[4] = type;
        if (payload != null) {
            for (int i = 0; i < payload.length; i++) {
                result[i + 5] = payload[i];
            }
        }
        return result;
    }

}
