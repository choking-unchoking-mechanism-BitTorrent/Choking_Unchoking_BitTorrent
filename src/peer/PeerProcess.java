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

import java.io.*;
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
    private int pieceNum;
    private HashMap<Integer, Peer> peers;
    private HashMap<Integer, Peer> preferredNeighbors;
    private HashMap<Integer ,BitField> bitFields;
    private HashMap<Integer, Connection> connectionHashMap;
    //Those who are interested on my data.
    private HashMap<Integer, Integer> interestPeer;
    private ArrayList<Integer> interestedPieces;
    private ArrayList<Integer> notInterestedPieces;
    private HashSet<Integer> requestingPeices;
    private PeerInfo me;
    private Timer timer;
    private TimerTask updatePreferredNeighbors;
    private TimerTask updateOptimisticNeighbor;
    private TimerTask checkALlFinished;

    public PeerProcess(int peerId) {

        this.peerId = peerId;
        this.preferredNeighbors = new HashMap<>();
        this.timer = new Timer();
        this.interestedPieces = new ArrayList<>();
        this.notInterestedPieces = new ArrayList<>();
        this.requestingPeices = new HashSet<>();
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
        pieceNum = 0;
        this.peers = peers;
        this.preferredNeighbors = new HashMap<>();
        this.interestedPieces = new ArrayList<>();
        this.notInterestedPieces = new ArrayList<>();
        this.requestingPeices = new HashSet<>();
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
            //Error here
            int piecesNumber = (int) Math.ceil((double) this.fileSize / this.pieceSize);
            pieceNum = piecesNumber;
            this.bitFields = new HashMap<>();
            this.peers = new HashMap<>();
            for (PeerInfo peerInfo : peerInfos) {
                //take out myself
                if (peerInfo.getHostID() == peerId) {
                    me = peerInfo;
                }
                this.peers.put(peerInfo.getHostID(), new Peer(peerInfo, piecesNumber));
            }
            BitField bitField = new BitField();
            if (me.getHasCompleteFile() > 0) {
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
            /*List<Peer> peerList = new ArrayList<>(peers.values());
            Collections.shuffle(peerList);
            for (int i = 0, j = 0; i < numberOfPreferredNeighbors;) {
                Peer peer = peerList.get(j++);
                if (peer.getPeerId() != peerId) {
                    preferredNeighbors.put(peer.getPeerId(), peer);
                    i++;
                }
            }*/

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
        updatePreferredNeighbors = new UpdatePreferredNeighbors();
        updateOptimisticNeighbor = new UpdateOptimisticNeighbor();
        checkALlFinished = new CheckAllThreadRunning();
        System.out.println(unchokingInterval);
        timer.schedule(updatePreferredNeighbors, 6000, this.unchokingInterval);
        timer.schedule(updateOptimisticNeighbor, 6000, this.optimisticUnchokingInterval);
        timer.schedule(checkALlFinished, 20000, 100);
        ServerSocket serverSocket = null;
        try{
            serverSocket = new ServerSocket(me.getPort());
        }catch(IOException e){
            e.printStackTrace();
        }
        for (Peer peer : peers.values()) {
            if (peer.getPeerId() < this.peerId){
                connect(peer);
            }
        }
        while (true) {
            try {
                System.out.println("Wait connecting");
                Socket receivedSocket = serverSocket.accept();
                String ip = receivedSocket.getRemoteSocketAddress().toString().split(":")[0].substring(1);
                System.out.println("Connected to " + ip);
                try {
                    Logger.connectedTCP(peerId);
                } catch (LoggerIOException e) {
                    e.printStackTrace();
                }
                Connection connection;
                for (Peer p : peers.values()) {
                    if (p.getHost().equals(ip) && p.getPeerId() != peerId) {
                        connection = new Connection(receivedSocket, this, p, peerId);
                        this.connectionHashMap.put(p.getPeerId(), connection);
                        //System.out.println("preferredNeighbors: " + preferredNeighbors.keySet().toString());
                                /*if (preferredNeighbors.containsKey(p.getPeerId())) {
                                    connection.setPreferN(true);
                                }*/
                        connection.start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect(Peer peer) {
        try {
            System.out.println(peer.getHost());
            Socket socket = new Socket(peer.getHost(), peer.getPort());
            Connection connection = new Connection(socket, this, peer, this.peerId);
            try {
                Logger.connectTCP(peer.getPeerId());
            } catch (LoggerIOException e) {
                e.printStackTrace();
            }
            System.out.println("Connected to " + peer.getPeerId());
            connectionHashMap.put(peer.getPeerId(), connection);
            /*if (preferredNeighbors.containsKey(peer.getPeerId()))
                connection.setPreferN(true);*/
            connection.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class UpdatePreferredNeighbors extends TimerTask {
        @Override
        public void run() {
            HashMap<Integer, Double> map = new HashMap<>();
            for (Map.Entry e : connectionHashMap.entrySet()){
                Connection c = (Connection) e.getValue();
                //TODO calculate rate here
                map.put((Integer)e.getKey(), (double)c.getDownloadBytes() / this.scheduledExecutionTime());
                c.doneCalculating();
            }

            preferredNeighbors.clear();
            for (int i = 0; i < numberOfPreferredNeighbors; i++){
                double max = -1;
                int id = -1;
                for (Map.Entry e : map.entrySet()){
                    //System.out.println("id : "+e.getKey());
                    //System.out.println("speed : "+e.getValue());
                    if ((Double)e.getValue() > max && interestPeer.containsKey(e.getKey())){
                        max = (Double)e.getValue();
                        id = (Integer)e.getKey();
                    }
                }
                if (max == -1 || id == -1){
                    return;
                }
                if (!connectionHashMap.get(id).getPreferN() && connectionHashMap.get(id).getIsRunning()){
                    connectionHashMap.get(id).setPreferN(true);
                }
                preferredNeighbors.put(id, peers.get(id));
                map.remove(id);
                //connectionHashMap.get(id).setSendRequest(true);
            }
            ArrayList<String> preferedNeighborList = new ArrayList<>();
            for(Integer i : preferredNeighbors.keySet()){
                preferedNeighborList.add(String.valueOf(i));
            }
            try {
                Logger.changePreferedNeighbors(preferedNeighborList);
            } catch (LoggerIOException e) {
                e.printStackTrace();
            }
            for (Map.Entry e : map.entrySet()){
                connectionHashMap.get(e.getKey()).setPreferN(false);
            }
        }

    }

    private class UpdateOptimisticNeighbor extends TimerTask {
        @Override
        public void run() {
            List<Connection> connections = new ArrayList<>();
            for (Map.Entry e : interestPeer.entrySet()){
                connectionHashMap.get((Integer)e.getKey()).setOpPrefer(false);
                if (!connectionHashMap.get((Integer)e.getKey()).getPreferN()){
                    if (!connectionHashMap.get((Integer)e.getKey()).getOpPrefer()){
                        connections.add(connectionHashMap.get((Integer)e.getKey()));
                    }
                }
            }
            //Get random index.
            if (connections.size() == 0){
                return;
            }
            Random rand = new Random();
            int randIndex = rand.nextInt(connections.size());
            connections.get(randIndex).setOpPrefer(true);
            try {
                Logger.changeOptimisticallyUnchokedNeighbor(connections.get(randIndex).getPeer().getPeerId());
            } catch (LoggerIOException e) {
                e.printStackTrace();
            }
        }
    }
    private class CheckAllThreadRunning extends TimerTask{
        @Override
        public void run(){
            for (Peer peer : peers.values()){
                if (!peer.getHasCompleteFile()){
                    return;
                }
            }
            try {
                Logger.closeLogger();
            } catch (LoggerIOException e) {
                e.printStackTrace();
            }
            System.out.println("All finished!");
            System.exit(0);
        }
    }
    //Put bit field.
    public synchronized void addBitField(BitField bitField, int peerID){
        if (!bitFields.containsKey(peerID)){
            bitFields.put(peerID, bitField);
            byte[] bytes = bitField.getBitFieldByteArray();
            for (byte b : bytes){
                if ((b & 0xFF) != 0xFF){
                    return;
                }
            }
            peers.get(peerID).setHasCompleteFile(true);
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
            int result = fs.read(file);
            System.out.println("Bytes of file : " + result);
            fs.close();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
    public synchronized byte[] getFilePart(int fileIndex){
        //start from 0.
        int start = fileIndex * pieceSize;
        //Last piece
        if (fileIndex + 1== pieceNum){
            return Arrays.copyOfRange(file, start, fileSize);
        }
        return Arrays.copyOfRange(file, start, start + pieceSize);
    }
    public synchronized void writeIntoFile(byte[] partOfFile, int index) throws IOException{
        boolean finish = true;
        for (int i = 0; i < partOfFile.length; i++){
            file[i+index*pieceSize] = partOfFile[i];
        }
        BitField bitField = this.getBitField(peerId);
        byte[] bitFieldByteArray = bitField.getBitFieldByteArray();
        for(int i = 0; i < bitFieldByteArray.length - 5; i++){
            if((bitFieldByteArray[i+5]) != (byte)255){
                finish = false;
            }
        }
        if(finish){
            try {
                Logger.completeDownload();
            } catch (LoggerIOException e) {
                e.printStackTrace();
            }
            System.out.println("The file complete!");
            FileOutputStream fileOutputStream = new FileOutputStream("out/" + fileName);
            fileOutputStream.write(file, 0, fileSize);
            fileOutputStream.close();
        }
    }
    public synchronized void sendHave(int index){
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
            if(!peer.getHasCompleteFile()){
                return false;
            }
        }
        return true;
    }
    public static byte[] getBooleanArray(byte b) {
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }
    public synchronized void updateBitField(int pieceIndex, int peerId){
        BitField bitField = bitFields.get(peerId);
        System.out.println("we will update bit field for pieceIndex: " + pieceIndex);
        bitField.updateBitField(pieceIndex);
        byte[] bitFieldByteArray = bitField.getBitFieldByteArray();
        System.out.println("bitfield length: " + bitFieldByteArray.length);
        System.out.println("payload: " + Arrays.toString(getBooleanArray(bitFieldByteArray[5])));
        for(int i = 0; i < bitFieldByteArray.length-5; i++){
            if(bitFieldByteArray[i+5] != (byte)255){
                return;
            }
        }
        System.out.println("peer: " + peerId + "become completed");
        peers.get(peerId).setHasCompleteFile(true);
        //TODO update bit field here, then decide if we should send interest
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
        System.out.println("Interest peer : ");
        for (Map.Entry e : interestPeer.entrySet()){
            System.out.println(e.getValue());
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

    public ArrayList<Integer> getInterestedPieces() {
        return interestedPieces;
    }

    public ArrayList<Integer> getNotInterestedPieces() {
        return notInterestedPieces;
    }

    public HashSet<Integer> getRequestingPeices() {
        return requestingPeices;
    }

    public HashMap<Integer, Integer> getInterestPeer() {
        return interestPeer;
    }

    public static void main(String[] args) {
        int peerId = Integer.parseInt(args[0]);

        //analyse config file
        PeerProcess peerProcess = new PeerProcess(peerId);
        peerProcess.run();
    }

}
