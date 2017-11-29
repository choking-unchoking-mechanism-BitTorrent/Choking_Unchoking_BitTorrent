package peer;

import exception.message.MessageException;
import logger.Logger;
import message.*;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

public class Connection extends Thread {
    private Socket socket;
    private Peer peer;
    private int myPeerID;
    private BufferedOutputStream outputStream;
    private BufferedInputStream inputStream;
    private boolean preferN;
    private boolean opPrefer;
    private boolean interested;
    private boolean broadcastHave;
    private int lastReceive;
    //Access peer process
    private PeerProcess process;
    private int downloadBytes;
    private int downloadSpeed;
    private boolean unchoke;

    public Connection(Socket socket, int myPeerID) {
        this.socket = socket;
        this.myPeerID = myPeerID;
        this.preferN = false;
        this.opPrefer = false;
        this.interested = false;
        this.broadcastHave = false;
        unchoke = false;
        try {
            this.outputStream = new BufferedOutputStream(this.socket.getOutputStream());
            this.inputStream = new BufferedInputStream(this.socket.getInputStream());
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
        unchoke = false;
        try {
            this.outputStream = new BufferedOutputStream(this.socket.getOutputStream());
            this.inputStream = new BufferedInputStream(this.socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setPreferN(boolean preferN){
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
            System.out.println(peer.getPeerId());
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
        //if(process.getInterestPeer().containsKey(peer.getPeerId())){
        Message requestMessage = new Message(MessageConstant.REQUEST_LENGTH, MessageConstant.REQUEST_TYPE,
                getRandomPieceIndex());
        System.out.println("send request to Peer: " + peer.getPeerId());
        return send(requestMessage);
        //} else {
        //    return false;
        //}
    }

    private boolean sendPiece(int pieceID){
        byte[] pieceContent = process.getFilePart(pieceID);
        byte[] pieceNum = ByteBuffer.allocate(4).putInt(pieceID).array();
        byte[] content = new byte[pieceContent.length + pieceNum.length];
        for (int i = 0; i < 4; i++){
            content[i] = pieceNum[i];
        }
        for (int i = 0; i < pieceContent.length; i++){
            content[i+4] = pieceContent[i];
        }
        //4 bytes length + 1 byte type + 4 byte index + length
        Message pieceMessage = new Message(pieceContent.length + 9, MessageConstant.PIECE_TYPE,
                content);
        System.out.println("Send piece index: " + pieceID + ", length : " + (process.getPieceSize() + 5));
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
            //TODO
            //Problem here : if complete, all bit field will be set to 1 include unused bit field.
            //if not complete, all bit filed set to 0 include unused bit field.
            //Ex. 4 pieces here but we get 8 interested because 1002 has 8 bits 0 and 1001 has 8 bits 1.
            //In fact, 1002's bit field should be 00001111.
            for (int j = 7; j > -1; j--) {
                if ((((1&0xFF) << j) & myByte) == 0 && (((1&0xFF) << j) & neighborByte) != 0) {
                    isInterested = true;
                    //Not exist.
                    if (process.getInterestedPieces().indexOf((i-5) * 8 + 7 - j) == -1){
                        process.getInterestedPieces().add((i-5) * 8 + 7 - j);
                    }
                } else if(process.getNotInterestedPieces().contains(new Integer((i-5) * 8 + 7 - j))) {
                    process.getNotInterestedPieces().remove(new Integer((i-5) * 8 + 7 - j));
                }
            }
        }
        this.interested = isInterested;
        System.out.println("myByte: " + bytesToHex(myBitfieldArray));
        System.out.println("neighborByte: " + bytesToHex(neighborBitfieldArray));
        System.out.println("Interested pieces : " + process.getInterestedPieces().size());
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
        HashSet<Integer> set = process.getRequestingPeices();
        int requestedPieceIndex;
        do {
            //System.out.println(this.process.getInterestedPieces().size());
            requestedPieceIndex = this.process.getInterestedPieces().get(new Random().
                    nextInt(this.process.getInterestedPieces().size()));
        } while (set.contains(requestedPieceIndex));
        System.out.println("Request peice index " + requestedPieceIndex);
        set.add(requestedPieceIndex);
        return ByteBuffer.allocate(4).putInt(requestedPieceIndex).array();
    }

    public synchronized void doneCalculating(){
        downloadBytes = 0;
    }
    public synchronized boolean getPreferN(){
        return preferN;
    }
    public synchronized boolean getOpPrefer(){
        return opPrefer;
    }
    public synchronized void setOpPrefer(boolean opPrefer){
        this.opPrefer = opPrefer;
    }
    public synchronized int getDownloadBytes(){
        return downloadBytes;
    }
    public synchronized void setUnchoke(boolean b){
        unchoke = b;
    }
    @Override
    public void run() {
        //Handshake first
        if (!handshake()){
            System.out.println("Handshake failed");
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
        sendChocked();

        while (true){
            //Send
            if (broadcastHave){
                sendHave();
                broadcastHave = false;
            }
            if (preferN || opPrefer){
                //sendPiece();
                //Send piece
            }
            if (unchoke){
                sendUnchocked();
                preferN = true;
                unchoke = false;
            }
            if (process.ifAllPeersComplete()){
                System.out.println("All peers finish!");
                break;
            }
            //receive
            //read length first.
            byte[] reply = new byte[4];
            try{
                int result = 0;
                if (inputStream.available() > 0)
                    result = inputStream.read(reply);
                else
                    continue;
//                if (inputStream.available() > 0) result = inputStream.read(reply);
                //if data arrived
                while(result == 4){
                    int length = 0;
//                    for (int i = 0; i < 4; i++){
//                        length += reply[i] << (3-i);
//                    }
                    //for (int i = 0; i < 4; i++){
                    length = reply[3] & 0xFF |
                            (reply[2] & 0xFF) << 8 |
                            (reply[1] & 0xFF) << 16 |
                            (reply[0] & 0xFF) << 24;
                        //}
                    length--;
                    byte[] type = new byte[1];
                    result = inputStream.read(type);
                    if (result != 1){
                        throw new MessageException();
                    }
                    byte[] payload = null;
                    if (length > 0){
                        System.out.println(length);
                        payload = new byte[length];
                        result = inputStream.read(payload);
                        if (result != length){
                            System.out.println("Wrong result");
                            throw new MessageException();
                        }
                    }
                    /*byte[] payload = new byte[length];
                    result = inputStream.read(payload);
                    length = 5 + result;
                    if (result != length){
                        System.out.println("Wrong result");
                        throw new MessageException();
                    }*/
                    switch (type[0]) {
                        case MessageConstant.UNCHOKE_TYPE:
                            System.out.println("Received unchocked from " + peer.getPeerId());
                            sendRequest();
                            break;
                        case MessageConstant.CHOKE_TYPE:
                            System.out.println("Received chocked from " + peer.getPeerId());
                            break;
                        case MessageConstant.HAVE_TYPE:
                            //Update here
                            int pieceNum1 = 0;
                            //for (int i = 0; i < 4; i++) {
                                pieceNum1 += payload[3] & 0xFF |
                                        (payload[2] & 0xFF) << 8 |
                                        (payload[1] & 0xFF) << 16 |
                                        (payload[0] & 0xFF) << 24;
                            //}
                            process.updateBitField(pieceNum1, peer.getPeerId());
                            if (isInterested(peer.getPeerId())) {
                                sendInterested();
                            } else {
                                sendNotInterested();
                            }
                            break;
                        case MessageConstant.REQUEST_TYPE:
                            System.out.println("Receive request from Peer: " + peer.getPeerId());
                            int pieceNum2 = 0;
                           // for (int i = 0; i < payload.length; i++) {
                                pieceNum2 += payload[3] & 0xFF |
                                        (payload[2] & 0xFF) << 8 |
                                        (payload[1] & 0xFF) << 16 |
                                        (payload[0] & 0xFF) << 24;
                            //}
                            System.out.println("Receive request for piece: " + pieceNum2);
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
                            int pieceNum3 = payload[3] & 0xFF |
                                    (payload[2] & 0xFF) << 8 |
                                    (payload[1] & 0xFF) << 16 |
                                    (payload[0] & 0xFF) << 24;
                            System.out.print("Receive piece index: " + pieceNum3);
                            //Update my bit field
                            process.updateBitField(pieceNum3, myPeerID);
                            process.writeIntoFile(Arrays.copyOfRange(payload, 4, payload.length), pieceNum3);
                            if (isInterested(peer.getPeerId())) {
                                //after receive a piece, continue to send request
                                sendRequest();
                            } else {
                                sendNotInterested();
                            }
                            downloadBytes += length;
                            //Send piece to file
                            process.writeIntoFile(Arrays.copyOfRange(payload, 5, payload.length), pieceNum3);
                            break;
                        default:
                            System.out.println("Unexpected message");
                            break;
                    }
                    if (inputStream.available() > 0)
                        result = inputStream.read(reply);
                    else
                        break;
                }
            }catch(IOException e){
                e.printStackTrace();
            }catch (MessageException e){
                e.printStackTrace();
            }
        }
        try{
            System.out.println("close stream");
            outputStream.close();
            inputStream.close();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
    }
}
