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
    private static byte type = 5;
    public static byte[] bitfield;

    public static void setBitfield(boolean hasFile, int piecesNum){
        hasCompleteFile = hasFile;
        totalPieces = piecesNum;
        int payloadLength = (int)Math.ceil((double)totalPieces/8);
        int remaining = totalPieces % 8;
        messageLength = ByteBuffer.allocate(4).putInt(payloadLength).array();
        payload = new byte[payloadLength];
        bitfield = new byte[payloadLength + 5];

        int i = 0;
        for(; i < messageLength.length; i++) {
            bitfield[i] = messageLength[i];
        }

        bitfield[i] = type;
        if(hasCompleteFile == false) {
            for(int j = 0; j < payload.length; j++) {
                i++;
                bitfield[i] = 0;
            }
        }else {
            i++;
            for(int k = 0; k < 8; k++){
                bitfield[i] = (byte) (bitfield[i] | 1 << k);
            }

            i++;
            for(int j = 0; j< remaining; j++) {
                bitfield[i] = (byte) (bitfield[i] | (1 << (7 - j)));
            }
        }
    }

    public static void updateBitField(int pieceIndex){
        int i = (pieceIndex - 1) / 8;
        int m = 7 - ((pieceIndex - 1) % 8);
        bitfield[i + 5] = (byte) (bitfield[i + 5] | (1 << m));
    }
}
