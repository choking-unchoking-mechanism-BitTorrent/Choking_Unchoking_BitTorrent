package message;

import java.nio.ByteBuffer;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Piece {
    public byte[] piece;
    private byte[] messageLength = new byte[4];
    private final static byte type = 7;
    private byte[] pieceIndex = new byte[4];
    private byte[] content;

    public Piece(int index, byte[] data) {
        content = data;
        int dataLength = content.length;
        pieceIndex = ByteBuffer.allocate(4).putInt(index).array();
        messageLength = ByteBuffer.allocate(4).putInt(4 + dataLength).array();
        piece = new byte[9 + dataLength];

        int i = 0;
        for(; i < messageLength.length; i++){
            piece[i] = messageLength[i];
        }

        piece[i] = type;

        for(int j = 0; j < pieceIndex.length; j++) {
            i++;
            piece[i] = pieceIndex[j];
        }

        for(int j = 0; j < content.length; j++) {
            i++;
            piece[i] = content[j];
        }
    }
}
