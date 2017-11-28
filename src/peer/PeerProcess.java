package peer;

import analyzer.CommonAnalyzer;
import analyzer.Constant;
import analyzer.PeerInfo;
import analyzer.PeerInfoAnalyzer;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import exception.analyzer.AnalyzerException;
import exception.logger.LoggerIOException;
import logger.Logger;
import message.BitField;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

public class PeerProcess {
    private int peerId;
    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private byte[] file;
    private int fileSize;
    private int pieceSize;
    private HashMap<Integer, Peer> peers;
    private HashMap<Integer, Peer> preferredNeighbors;
    private HashMap<Integer ,BitField> bitFields;
    private HashMap<Integer, Connection> connectionHashMap;
    private HashMap<Integer, Integer> interestPeer;
    private PeerInfo me;
    private Timer timer;

    public PeerProcess(int peerId) {

        this.peerId = peerId;
        this.preferredNeighbors = new HashMap<>();
        this.timer = new Timer();
    }

    public PeerProcess(
            int peerId,
            int numberOfPreferredNeighbors,
            int unchokingInterval, int optimisticUnchokingInterval,
            String fileName,
            int fileSize,
            int pieceSize,
            HashMap<Integer, Peer> peers) {
        this.peerId = peerId;
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.peers = peers;
        this.preferredNeighbors = new HashMap<>();
    }

    private void init() {
        CommonAnalyzer commonAnalyzer = new CommonAnalyzer();
        PeerInfoAnalyzer peerInfoAnalyzer = new PeerInfoAnalyzer();
        try {
            HashMap<String, Object> configs = commonAnalyzer.analyze();
            this.numberOfPreferredNeighbors = (int) configs.get(Constant.STRING_NUMBER_OF_PREFER_NEIGHBOURS);
            this.unchokingInterval = (int) configs.get(Constant.STRING_UNCHOKING_INTERVAL);
            this.optimisticUnchokingInterval = (int) configs.get(Constant.STRING_OPTIMISTIC_UNCHOKING_INTERVAL);
            this.fileName = (String) configs.get(Constant.STRING_FILE_NAME);
            this.fileSize = (int) configs.get(Constant.STRING_FILE_SIZE);
            this.pieceSize = (int) configs.get(Constant.STRING_PIECE_SIZE);
            this.file = new byte[this.fileSize];
            this.interestPeer = new HashMap<>();
            try {
                Logger.initLogger(peerId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<PeerInfo> peerInfos = peerInfoAnalyzer.analyze();
            int piecesNumber = (int) Math.ceil((double) this.fileSize / this.pieceSize);
            this.bitFields = new HashMap<>();
            this.peers = new HashMap<>();
            for (PeerInfo peerInfo : peerInfos) {
                this.peers.put(peerInfo.getHostID(), new Peer(peerInfo, piecesNumber));
                //Get me
                if (peerInfo.getHostID() == peerId){
                    me = peerInfo;
                }

            }
            BitField bitField = new BitField();
            if (me.getHasCompleteFile() > 0) {
                System.out.println("HasCompleteFile: " + me.getHasCompleteFile());
                bitField.setBitField(true, piecesNumber);
                this.bitFields.put(this.peerId, bitField);
            } else {
                bitField.setBitField(false, piecesNumber);
                this.bitFields.put(this.peerId, bitField);
            }
            for (Peer p : peers.values()){
                System.out.println(p.getPeerId());
            }
            //init preferredNeighbors
            List<Peer> peerList = new ArrayList<>(peers.values());
            Collections.shuffle(peerList);
            for (int i = 0; i < numberOfPreferredNeighbors; i++) {
                Peer peer = peerList.get(i);
                preferredNeighbors.put(peer.getPeerId(), peer);
            }

            //I have complete file.
            if (me.getHasCompleteFile() == 1){
                setTheFile();
            }
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        init();
        //build connections
        this.connectionHashMap = new HashMap<>();
        for (Peer peer : peers.values()) {
            if (peer.getPeerId() == this.peerId){
                while (true) {
                    try {
                        ServerSocket serverSocket = new ServerSocket(me.getPort());
                        System.out.println("Waiting connecting");
                        Socket receivedSocket = serverSocket.accept();
                        String ip = receivedSocket.getRemoteSocketAddress().toString().split(":")[0].substring(1);
                        System.out.println(ip);
                        Connection connection;
                        for (Peer p : peers.values()) {
                            if (p.getHost().equals(ip)) {
                                connection = new Connection(receivedSocket, this, p, peerId);
                                this.connectionHashMap.put(peer.getPeerId(), connection);
                                if (preferredNeighbors.containsKey(peer.getPeerId()))
                                    connection.setPreferN(true);
                                connection.start();
                            }
                        }
                    } catch (IOException e) {
                        //TODO
                    }
                }
            }
            connect(peer);
        }
        TimerTask updatePreferredNeighbors = new UpdatePreferredNeighbors();
        TimerTask updateOptimisticNeighbor = new UpdateOptimisticNeighbor();
        timer.schedule(updatePreferredNeighbors, 0, this.unchokingInterval);
        timer.schedule(updateOptimisticNeighbor, 0, this.optimisticUnchokingInterval);
    }

    public void connect(Peer peer) {
        try {
            System.out.println(peer.getHost());
            Socket socket = new Socket(peer.getHost(), peer.getPort());
            Connection connection = new Connection(socket, this, peer, this.peerId);
            Logger.connectTCP(peer.getPeerId());
            System.out.println("Connected to " + peer.getPeerId());
            connectionHashMap.put(peer.getPeerId(), connection);
            if (preferredNeighbors.containsKey(peer.getPeerId()))
                connection.setPreferN(true);
            connection.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LoggerIOException e) {
            //TODO
        }
    }

    private class UpdatePreferredNeighbors extends TimerTask {
        @Override
        public void run() {
            //TODO
        }
    }

    private class UpdateOptimisticNeighbor extends TimerTask {
        @Override
        public void run() {
            //TODO
        }
    }
    //Put bit field.
    public synchronized void addBitField(BitField bitField, int peerID){
        if (!bitFields.containsKey(peerID)){
            bitFields.put(peerID, bitField);
        }
    }
    //Get bit field
    public synchronized BitField getBitField(int peerID){
        if (bitFields.containsKey(peerID)){
            return bitFields.get(peerID);
        }
        return null;
    }
    //Set file array
    private synchronized boolean setTheFile(){
        File f = new File(fileName);
        try{
            FileInputStream fs = new FileInputStream(f);
            fs.read(file);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public synchronized byte[] getFilePart(int fileIndex){
        //start from 0.
        int start = fileIndex * pieceSize;
        return Arrays.copyOfRange(file, start, start + pieceSize);
    }
    public synchronized void writeIntoFile(byte[] partOfFile, int index){
        for (int i = 0; i < partOfFile.length; i++){
            file[i+index*pieceSize] = partOfFile[i];
        }
    }
    public void sendHave(int index){
        for (HashMap.Entry e : connectionHashMap.entrySet()){
            Connection c =(Connection)e.getValue();
            c.broadcastHave(index);
        }
    }
    public int getPieceSize(){
        return pieceSize;
    }
    public synchronized int getHasCompleteFile(){
        return me.getHasCompleteFile();
    }
    public synchronized boolean ifAllPeersComplete(){
//        for (int i = 0; i < peers.size(); i++){
//            if (!peers.get(i).getHasCompleteFile()){
//                return false;
//            }
//        }
        for (Peer peer : peers.values()) {
            if(peer.getHasCompleteFile())
                return false;
        }
        return true;
    }
    public synchronized void updateBitField(int pieceIndex, int peerId){
        BitField bitField = bitFields.get(peerId);
        bitField.updateBitField(pieceIndex);
        //TODO update bit field here, then decide if we should send interest

    }
    //Get a random number of piece from peerId have but me don't have
    public synchronized byte[] getRandomPieceIndex(int peerId){
        //TODO
        Random random = new Random();
        return ByteBuffer.allocate(4).putInt(random.nextInt()).array();
    }
    public synchronized void updateInterestPeer(int peerId, boolean isInterest){
        if (interestPeer.containsKey(peerId)){
            if (!isInterest){
                interestPeer.remove(peerId);
            }
        }else {
            if (isInterest){
                interestPeer.put(peerId, peerId);
            }
        }
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

    public static void main(String[] args) {
        int peerId = Integer.parseInt(args[0]);

        //analyse config file
        PeerProcess peerProcess = new PeerProcess(peerId);
        peerProcess.run();
    }

}
