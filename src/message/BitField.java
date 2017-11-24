package message;

import java.nio.ByteBuffer;

/**
 * Created by qiaochu on 10/25/17.
 */
public class BitField {

    private static boolean hasCompleteFile = false;
    private static int totalPieces;
    private static byte[] payload;
    private static byte[] messageLength = new byte[4];
    private final static byte type = 5;
    public static byte[] bitField;

    public synchronized static void setBitField(boolean hasFile, int piecesNum){
        hasCompleteFile = hasFile;
        totalPieces = piecesNum;
        int payloadLength = (int)Math.ceil((double)totalPieces/8);
        int remaining = totalPieces % 8;
        messageLength = ByteBuffer.allocate(4).putInt(payloadLength).array();
        payload = new byte[payloadLength];
        bitField = new byte[payloadLength + 5];

        int i = 0;
        for(; i < messageLength.length; i++) {
            bitField[i] = messageLength[i];
        }

        bitField[i] = type;
        if(hasCompleteFile == false) {
            for(int j = 0; j < payload.length; j++) {
                i++;
                bitField[i] = 0;
            }
        }else {
            i++;
            for(int k = 0; k < 8; k++){
                bitField[i] = (byte) (bitField[i] | 1 << k);
            }

            i++;
            for(int j = 0; j< remaining; j++) {
                bitField[i] = (byte) (bitField[i] | (1 << (7 - j)));
            }
        }
    }

    public static byte[] getBitFieldByteArray(){
        byte[] array = new byte[5 + bitField.length];
        for (int i = 0; i < 4; i++){
            array[i] = messageLength[i];
        }
        array[4] = type;
        for (int i = 0; i < bitField.length; i++){
            array[i+5] = bitField[i];
        }
        return bitField;
    }

    public static void updateBitField(int pieceIndex){
        int i = (pieceIndex - 1) / 8;
        int m = 7 - ((pieceIndex - 1) % 8);
        bitField[i + 5] = (byte) (bitField[i + 5] | (1 << m));
    }
}
