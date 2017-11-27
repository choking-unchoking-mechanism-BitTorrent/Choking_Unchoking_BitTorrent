package message;

/**
 * Created by qiaochu on 10/27/17.
 */
public class MessageConstant {
    public final static int CHOKE_LENGTH = 5;
    public final static byte CHOKE_TYPE = 0;
    public final static int HANDSHAKE_LENGTH = 32;
    public final static String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    public final static String HANDSHAKE_ZERO_BITS = "0000000000";
    public final static int HAVE_LENGTH = 9;
    public final static byte HAVE_TYPE = 4;
    public final static int INTERESTED_LENGTH = 5;
    public final static byte INTERESTED_TYPE = 2;
    public final static int NOT_INTERESTED_LENGTH = 5;
    public final static byte NOT_INTERESTED_TYPE = 3;
    public final static byte PIECE_TYPE = 7;
    public final static int REQUEST_LENGTH = 9;
    public final static byte REQUEST_TYPE = 6;
    public final static int UNCHOKE_LENGTH = 5;
    public final static byte UNCHOKE_TYPE = 1;
}