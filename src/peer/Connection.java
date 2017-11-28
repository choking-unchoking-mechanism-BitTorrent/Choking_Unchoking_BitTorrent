package peer;

import exception.message.MessageException;
import logger.Logger;
import message.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

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
            this.outputStream = this.socket.getOutputStream();
            this.inputStream = this.socket.getInputStream();
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
        this.downloadBytes = 0;
        this.downloadSpeed = 0;
        try {
            this.outputStream = this.socket.getOutputStream();
            this.inputStream = this.socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPreferN(boolean preferN){
        this.preferN = preferN;
    }

    private boolean send(Message message) {
        try{
            this.outputStream.write(message.getMessageByteArray());
            this.outputStream.flush();
        }catch(IOException e){
            //TODO
            return false;
        }
        return true;
    }
    private boolean handshake(){
        try{
            //Send handshake
            this.outputStream.write(new Handshake(myPeerID).getHandshake());
            this.outputStream.flush();
            //Wait for reply
            byte[] reply = new byte[MessageConstant.HANDSHAKE_LENGTH];
            int result = inputStream.read(reply);
            System.out.println("Result : " + result);
            while (result <= 0){
                System.out.println("waiting to handshake");
                result = this.inputStream.read(reply);
            }
            Handshake handshake = new Handshake(reply);
            System.out.println(handshake.getPeerID());
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
            System.out.println("Sent bitfield successfully");
            //wait for bit field
            byte[] reply = new byte[field.getBitFieldByteArray().length];
            inputStream.read(reply);
            BitField bitField = new BitField();
            bitField.setBitField(reply);
            process.addBitField(bitField, peer.getPeerId());
            System.out.println("Receive bitfield successfully");
            if(isInterested(peer.getPeerId())) {
                System.out.println("Interested!");
                sendInterested();
            } else {
                System.out.println("Not Interested!");
                sendNotInterested();
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean sendRequest(){
        Message requestMessage = new Message(MessageConstant.REQUEST_LENGTH, MessageConstant.REQUEST_TYPE,
                getRandomPieceIndex());
        return send(requestMessage);
    }

    private boolean sendPiece(int pieceID){
        Message pieceMessage = new Message(process.getPieceSize(), MessageConstant.PIECE_TYPE,
                process.getFilePart(pieceID));
        System.out.println("Send piece index: " + pieceID);
        return send(pieceMessage);
    }

    private boolean sendInterested(){
        Message interestedMsg = new Message(MessageConstant.INTERESTED_LENGTH, MessageConstant.INTERESTED_TYPE, null);
        System.out.println("Sent Interested!");
        return send(interestedMsg);
    }

    private boolean sendNotInterested(){
        Message notInterestedMsg = new Message(MessageConstant.NOT_INTERESTED_LENGTH, MessageConstant.NOT_INTERESTED_TYPE, null);
        System.out.println("Sent Not Interested!");
        return send(notInterestedMsg);
    }

    private boolean sendChocked() {
        Message chockedMsg = new Message(MessageConstant.CHOKE_LENGTH, MessageConstant.CHOKE_TYPE, null);
        return send(chockedMsg);
    }

    private boolean sendUnchocked(){
        Message unchockedMsg = new Message(MessageConstant.UNCHOKE_LENGTH, MessageConstant.UNCHOKE_TYPE, null);
        System.out.println("Unchocked peer " + peer.getPeerId());
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

    private synchronized boolean isInterested(int peerId) {
        Boolean isInterested = false;
        byte[] myBitfieldArray = process.getBitField(myPeerID).getBitFieldByteArray();
        byte[] neighborBitfieldArray = process.getBitField(peerId).getBitFieldByteArray();
        for (int i = 5; i < myBitfieldArray.length; i++) {
            byte myByte = myBitfieldArray[i];
            byte neighborByte = neighborBitfieldArray[i];
            for (int j = 7; j > -1; j--) {
                if (((1 << j) & myByte) == 0 && ((1 << j) & neighborByte) == 1) {
                    isInterested = true;
                    process.getInterestedPieces().add(i * 8 + 7 - j);
                } else if(process.getNotInterestedPieces().contains(new Integer(i * 8 + 7 - j))) {
                    process.getNotInterestedPieces().remove(new Integer(i * 8 + 7 - j));
                }
            }
        }
        this.interested = isInterested;
        System.out.println("myByte: " + bytesToHex(myBitfieldArray));
        System.out.println("neighborByte: " + bytesToHex(neighborBitfieldArray));
        return isInterested;
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    //Get a random number of piece from peerId have but me don't have
    private synchronized byte[] getRandomPieceIndex(){
        //TODO
        HashSet<Integer> set = process.getRequestingPeices();
        int requestedPieceIndex;
        do {
            requestedPieceIndex = this.process.getInterestedPieces().get(new Random().nextInt(this.process.getInterestedPieces().size()));
        } while (!set.contains(requestedPieceIndex));
        System.out.println("Request peice index " + requestedPieceIndex);
        set.add(requestedPieceIndex);
        return ByteBuffer.allocate(4).putInt(requestedPieceIndex).array();
    }

    @Override
    public void run() {
        //Handshake first
        if (!handshake()){
            System.out.println("Failed");
            return;
        }
        System.out.println("handshake successfully");
        try{
            Logger.initLogger(myPeerID);
            Logger.connectedTCP(myPeerID);
            Logger.closeLogger();
        }catch (Exception e){

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
                            if (isInterested(peer.getPeerId())) {
                                sendInterested();
                            } else {
                                sendNotInterested();
                            }
                            break;
                        case MessageConstant.REQUEST_TYPE:
                            int pieceNum2 = 0;
                            for (int i = 0; i < 4; i++){
                                pieceNum2 += reply[i] << (3-i);
                            }
                            sendPiece(pieceNum2);
                            break;
                        case MessageConstant.INTERESTED_TYPE:
                            System.out.println("Receive Interested from peer " + peer.getPeerId());
                            process.updateInterestPeer(peer.getPeerId(), true);
                            break;
                        case MessageConstant.NOT_INTERESTED_TYPE:
                            System.out.println("Receive Not Interested from peer " + peer.getPeerId());
                            process.updateInterestPeer(peer.getPeerId(), false);
                            break;
                        case MessageConstant.PIECE_TYPE:
                            //Get piece index
                            int pieceNum3 = 0;
                            for (int i = 0; i < 4; i++){
                                pieceNum3 += reply[i] << (3-i);
                            }
                            System.out.print("Receive piece index: " + pieceNum3);
                            //Update my bit field
                            process.updateBitField(pieceNum3, peer.getPeerId());
                            if (isInterested(peer.getPeerId())) {
                                //after receive a piece, continue to send request
                                sendRequest();
                            } else {
                                sendNotInterested();
                            }
                            downloadBytes+=length;
                            //Send piece to file
                            process.writeIntoFile(Arrays.copyOfRange(reply, 5, reply.length), pieceNum3);
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
