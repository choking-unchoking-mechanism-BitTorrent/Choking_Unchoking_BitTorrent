package peer;

import exception.message.MessageException;
import logger.Logger;
import message.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Connection extends Thread {
    private Socket socket;
    private Peer peer;
    private int myPeerID;
    private OutputStream outputStream;
    private InputStream inputStream;
    private boolean preferN;
    private boolean opPrefer;
    private boolean interested;
    private boolean broadcastHave;
    private int lastReceive;
    //Access peer process
    private PeerProcess process;
    private int downloadBytes;
    private int downloadSpeed;

    public Connection(Socket socket, int myPeerID) {
        this.socket = socket;
        this.myPeerID = myPeerID;
        this.preferN = false;
        this.opPrefer = false;
        this.interested = false;
        this.broadcastHave = false;
        try {
            this.outputStream = socket.getOutputStream();
            this.inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection(Socket socket, PeerProcess process, Peer peer, int myPeerID) {
        this.socket = socket;
        this.peer = peer;
        this.process = process;
        this.myPeerID = myPeerID;
        this.preferN = false;
        this.opPrefer = false;
        this.interested = false;
        this.broadcastHave = false;
        downloadBytes = 0;
        downloadSpeed = 0;
        try {
            this.outputStream = socket.getOutputStream();
            this.inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPreferN(boolean preferN){
        this.preferN = preferN;
    }

    private boolean send(Message message) {
        try{
            outputStream.write(message.getMessageByteArray());
            outputStream.flush();
        }catch(IOException e){
            //TODO
            return false;
        }
        return true;
    }
    private boolean handshake(){
        try{
            //Send handshake
            outputStream.write(new Handshake(myPeerID).getHandshake());
            outputStream.flush();
            //Wait for reply
            byte[] reply = new byte[MessageConstant.HANDSHAKE_LENGTH];
            int result = inputStream.read(reply);
            while (result <= 0){
                Logger.initLogger(myPeerID);
                Logger.connectTCP(myPeerID);
                System.out.println("waiting to connect");
                result = inputStream.read(reply);
            }
            Handshake handshake = new Handshake(reply);
            if (handshake.getPeerID() != peer.getPeerId()){
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private boolean sendBitfield(){
        try{
            BitField field = process.getBitField(this.myPeerID);
            outputStream.write(field.getBitFieldByteArray());
            outputStream.flush();
            //wait for bit field
            byte[] reply = new byte[field.getBitFieldByteArray().length];
            BitField bitField = new BitField();
            bitField.setBitField(reply);
            process.addBitField(bitField, peer.getPeerId());
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean sendRequest(){
        Message requestMessage = new Message(MessageConstant.REQUEST_LENGTH, MessageConstant.REQUEST_TYPE,
                process.getRandomPieceIndex(peer.getPeerId()));
        return send(requestMessage);
    }

    private boolean sendPiece(int pieceID){
        Message pieceMessage = new Message(process.getPieceSize(), MessageConstant.PIECE_TYPE,
                process.getFilePart(pieceID));
        return send(pieceMessage);
    }

    private boolean sendInterested(){
        Message interestedMsg = new Message(MessageConstant.INTERESTED_LENGTH, MessageConstant.INTERESTED_TYPE, null);
        return send(interestedMsg);
    }

    private boolean sendNotInterested(){
        Message notInterestedMsg = new Message(MessageConstant.NOT_INTERESTED_LENGTH, MessageConstant.NOT_INTERESTED_TYPE, null);
        return send(notInterestedMsg);
    }

    private boolean sendChocked() {
        Message chockedMsg = new Message(MessageConstant.CHOKE_LENGTH, MessageConstant.CHOKE_TYPE, null);
        return send(chockedMsg);
    }

    private boolean sendUnchocked(){
        Message unchockedMsg = new Message(MessageConstant.UNCHOKE_LENGTH, MessageConstant.UNCHOKE_TYPE, null);
        return send(unchockedMsg);
    }
    private boolean sendHave(){
        Message sendMsg = new Message(MessageConstant.HAVE_LENGTH, MessageConstant.HAVE_TYPE,
                ByteBuffer.allocate(4).putInt(lastReceive).array());
        return send(sendMsg);
    }
    public void broadcastHave(int received){
        broadcastHave = true;
        lastReceive = received;
    }
    @Override
    public void run() {
        try{
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        }catch (IOException e){
            e.printStackTrace();
            return;
        }
        //Handshake first
        if (!handshake()){
            return;
        }
        //Send Bitfield
        if (!sendBitfield()){
            return;
        }
        while (true){
            if (process.ifAllPeersComplete())
                break;
            //Send
            if (broadcastHave){
                sendHave();
                broadcastHave = false;
            }
            if (preferN || opPrefer){
                sendUnchocked();
                //Send piece
            } else {
                sendChocked();
            }
            //receive
            //TODO
            //read length first.
            byte[] reply = new byte[4];
            try{
                int result = inputStream.read(reply);
                //if data arrived
                while(result == 4){
                    int length = 0;
                    for (int i = 0; i < 4; i++){
                        length += reply[i] << (3-i);
                    }
                    byte[] type = new byte[1];
                    result = inputStream.read(type);
                    if (result != 1){
                        throw new MessageException();
                    }
                    byte[] payload = new byte[length];
                    result = inputStream.read(payload);
                    if (result != length){
                        throw new MessageException();
                    }
                    switch ((int)type[0]){
                        case MessageConstant.UNCHOKE_TYPE:
                            sendRequest();
                            break;
                        case MessageConstant.CHOKE_TYPE:
                            //Do nothing
                            break;
                        case MessageConstant.HAVE_TYPE:
                            //Update here
                            int pieceNum1 = 0;
                            for (int i = 0; i < 4; i++){
                                pieceNum1 += reply[i] << (3-i);
                            }
                            process.updateBitField(pieceNum1, peer.getPeerId());
                            break;
                        case MessageConstant.INTERESTED_TYPE:
                            process.updateInterestPeer(peer.getPeerId(), true);
                            break;
                        case MessageConstant.NOT_INTERESTED_TYPE:
                            process.updateInterestPeer(peer.getPeerId(),false);
                            break;
                        case MessageConstant.PIECE_TYPE:
                            //Get piece index
                            int pieceNum2 = 0;
                            for (int i = 0; i < 4; i++){
                                pieceNum2 += reply[i] << (3-i);
                            }
                            //Update my bit field
                            process.updateBitField(pieceNum2, peer.getPeerId());
                            downloadBytes+=length;
                            //Send piece to file
                            process.writeIntoFile(Arrays.copyOfRange(reply, 5, reply.length), pieceNum2);
                            break;
                    }
                    result = inputStream.read(reply);
                }
            }catch(IOException e){
                e.printStackTrace();
            }catch (MessageException e){
                e.printStackTrace();
            }
        }
        try{
            outputStream.close();
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
}
