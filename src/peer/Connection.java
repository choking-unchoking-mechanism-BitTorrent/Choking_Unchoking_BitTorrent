package peer;

import analyzer.PeerInfo;
import exception.*;
import exception.message.HandshakeMessageException;
import message.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection extends Thread {
    private Socket socket;
    private Peer peer;
    private int myPeerID;
    private OutputStream outputStream;
    private InputStream inputStream;
    private int downloadSpeed;

    public Connection(Socket socket, Peer peer, int myPeerID) {
        this.socket = socket;
        this.peer = peer;
        this.myPeerID = myPeerID;
        try {
            this.outputStream = socket.getOutputStream();
            this.inputStream = socket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            inputStream.read(reply);
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
            outputStream.write(BitField.getBitFieldByteArray());
            outputStream.flush();
            //wait for bitfield
            //TODO
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean sendPiece(){
        //TODO
        return false;
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
            //TODO
        }
        //Send Bitfield
        if (!sendBitfield()){
            //TODO
        }
    }
}
