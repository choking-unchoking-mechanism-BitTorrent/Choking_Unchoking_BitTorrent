package peer;

import analyzer.CommonAnalyzer;
import analyzer.Constant;
import analyzer.PeerInfo;
import analyzer.PeerInfoAnalyzer;
import exception.analyzer.AnalyzerException;

import java.util.*;

public class PeerProcess {
    private int peerId;
    private int numberOfPreferredNeighbors;
    private int unchokingInterval;
    private int optimisticUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;
    private ArrayList<Peer> peers;
    private ArrayList<Peer> preferredNeighbors;

    public PeerProcess(int peerId) {
        this.peerId = peerId;
    }

    public PeerProcess(
            int peerId,
            int numberOfPreferredNeighbors,
            int unchokingInterval, int optimisticUnchokingInterval,
            String fileName,
            int fileSize,
            int pieceSize,
            ArrayList<Peer> peers) {
        this.peerId = peerId;
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
        this.unchokingInterval = unchokingInterval;
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.peers = peers;
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

            List<PeerInfo> peerInfos = peerInfoAnalyzer.analyze();
            int piecesNumber = (int) Math.ceil((double) this.fileSize / this.pieceSize);
            this.peers = new ArrayList<>();
            for (PeerInfo peerInfo : peerInfos) {
                this.peers.add(new Peer(peerInfo, piecesNumber));
            }
        } catch (AnalyzerException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        init();
        //build connections
        //TO DO...

        new Timer().schedule(new updatePreferredNeighbors(), 0, this.unchokingInterval);
        new Timer().schedule(new updateOptimisticNeighbor(), 0, this.optimisticUnchokingInterval);
    }

    public boolean connect(int peerId) {

        return false;
    }

    private class updatePreferredNeighbors extends TimerTask {
        @Override
        public void run() {

        }
    }

    private class updateOptimisticNeighbor extends TimerTask {
        @Override
        public void run() {

        }
    }

    public static void main(String[] args) {
        int peerId = Integer.parseInt(args[1]);

        //analyse config file
        PeerProcess peerProcess = new PeerProcess(peerId);
        peerProcess.run();
    }

}
