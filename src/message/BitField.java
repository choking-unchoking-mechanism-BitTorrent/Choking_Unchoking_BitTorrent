package message;

import peer.Connection;
import peer.PeerProcess;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by qiaochu on 10/25/17.
 */
//Change from static to non static
public class BitField {

    private  boolean hasCompleteFile = false;
    private  int totalPieces;
    private  byte[] payload;
    private  byte[] messageLength = new byte[4];
    private final static byte type = 5;
    public  byte[] bitField;
    private String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    public synchronized void setBitField(byte[] content){
        int num = 0;
        for (int i = 0; i < 4; i++){
            messageLength[i] = content[i];
            num += (int)content[i] << (3-i);
        }
        //num = java.nio.ByteBuffer.wrap(Arrays.copyOfRange(content, 0, 5)).getInt();
        System.out.println(num);
        //Ignore content[4] which it type
        //content[4] == (byte)5
        //System.out.println(num);
        bitField = new byte[num + 5];
        bitField = content;
        payload = new byte[num];
        System.out.println("debug: " + bytesToHex(bitField));

        for (int i = 0; i < num; i++){
            payload[i] = content[5+i];
        }
    }
    public void setBitField(boolean hasFile, int piecesNum){
        hasCompleteFile = hasFile;
        totalPieces = piecesNum;
        int payloadLength = (int)Math.ceil((double)totalPieces/8);
        int remaining = totalPieces % 8;
        messageLength = ByteBuffer.allocate(4).putInt(payloadLength).array();
        payload = new byte[payloadLength];
        bitField = new byte[payloadLength + 5];

        int i = 0;
        for(; i < 4; i++) {
            bitField[i] = messageLength[i];
        }

        bitField[i] = type;
        if(!hasCompleteFile) {
            for(int j = 0; j < payload.length - 1; j++) {
                i++;
                bitField[i] = 0;
            }
            if (remaining == 0){
                return;
            }
            byte lastByte = 0;
            for (int j = 0; j < 8 - remaining; j++){
                lastByte += ((1&0xFF) << (j));
            }
            bitField[++i] = lastByte;
        }else {
            for(int k = 0; k < payloadLength; k++){
                bitField[++i] = (byte) 0xFF;
            }
        }
    }

    public byte[] getBitFieldByteArray(){
//        byte[] array = new byte[5 + bitField.length];
//        for (int i = 0; i < 4; i++){
//            array[i] = messageLength[i];
//        }
//        array[4] = type;
//        for (int i = 0; i < bitField.length; i++){
//            array[i+5] = bitField[i];
//        }
        return bitField;
    }
    public byte[] getPayload() {
        return payload;
    }
    public void updateBitField(int pieceIndex){
        int i = (pieceIndex) / 8;
        System.out.println("The bitfield before updated is: " + Arrays.toString(getBooleanArray(bitField[i + 5])));

        int m = 7 - ((pieceIndex) % 8);
        bitField[i + 5] = (byte) (bitField[i + 5] | ((1 & 0xFF) << m));
        System.out.println("The updated bitfield is: " + Arrays.toString(getBooleanArray(bitField[i + 5])));

    }
    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }
}
