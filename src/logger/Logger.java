package logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import exception.logger.LoggerIOException;

/**
 * Created by qiaochu on 10/25/17.
 */
public class Logger {

    private static int myPeerID;
    private static FileWriter logFile;
    private static BufferedWriter bWriter;

    private final static String LOG_FILE_NAME_HEADER = "log_peer_";
    private final static String LOG_FILE_SUFFIX = ".log";
    private final static String INIT_LOGGER_ERROR = " Can not open log file";
    private final static String FAIL_IN_WRITING_LOG = " Fail in writing log";
    private final static String CLOSE_LOGGER_ERROR =" Can not close log file";
    private final static String TCP_CONNECTING_LOG = " makes a connection to Peer ";
    private final static String TCP_CONNECTED_LOG = " is connected from Peer ";
    private final static String CHANGE_PREFERED_NEIGHBORS_LOG = " has the prefered neighbors ";
    private final static String CHANGE_OPTIMISTICALLY_UNCHOKED_NEIGHBOR_LOG = " has the optimistically unchoked neighbor ";
    private final static String UNCHOKING_LOG = " is unchoked by ";
    private final static String CHOKING_LOG = " is choked by ";
    private final static String RECEIVE_HAVE_LOG = " received the 'have' message from ";
    private final static String RECEIVE_INTERESTED_LOG = " received the 'interested' message from ";
    private final static String RECEIVE_NOT_INTERESTED_LOG = " received the ‘not interested’ message from ";
    private final static String DOWNLOAD_PIECE = " has downloaded the piece ";
    private final static String COMPLETE_DOWNLOAD = " has downloaded the complete file";
    private final static String PEER_ID = ": Peer ";
    private final static String FOR_PIECE = " for the piece ";

    public static void initLogger(int peerID) throws LoggerIOException {
        String prefix = "initLogger";
        myPeerID = peerID;
        String fileName = LOG_FILE_NAME_HEADER + myPeerID + LOG_FILE_SUFFIX;
        try {
            logFile = new FileWriter(fileName);
            bWriter = new BufferedWriter(logFile);
        } catch (IOException ex) {
            String message = prefix + INIT_LOGGER_ERROR;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void connectTCP (int peerID) throws LoggerIOException {
        String prefix = "connectTCP";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + TCP_CONNECTING_LOG + peerID;
            bWriter.write(log);
            bWriter.newLine();
        } catch(IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void connectedTCP (int peerID) throws LoggerIOException {
        String prefix = "connectedTCP";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + TCP_CONNECTED_LOG + peerID;
            bWriter.write(log);
            bWriter.newLine();
        } catch(IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void changePreferedNeighbors (ArrayList<String> neighborsPeerID)
            throws LoggerIOException {
        String prefix = "changePreferedNeighbors";
        StringBuilder stringBuilder = new StringBuilder();
        for (String neigborPeerID : neighborsPeerID) {
            stringBuilder.append(neigborPeerID);
            stringBuilder.append(";");
        }
        String neighborsPeerIDList = stringBuilder.toString().substring(0, (stringBuilder.toString().length() - 1));
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + CHANGE_PREFERED_NEIGHBORS_LOG + neighborsPeerIDList;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void changeOptimisticallyUnchokedNeighbor
            (int optimisticallyUnchokedNeighborID) throws LoggerIOException {
        String prefix = "changeOptimisticallyUnchokedNeighbor";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + CHANGE_OPTIMISTICALLY_UNCHOKED_NEIGHBOR_LOG + optimisticallyUnchokedNeighborID;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void unchoke (int peerID) throws LoggerIOException {
        String prefix = "unchoke";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + UNCHOKING_LOG + peerID;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void choke (int peerID) throws LoggerIOException {
        String prefix = "choke";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + CHOKING_LOG + peerID;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void receiveHave (int peerID, int pieceIndex) throws LoggerIOException {
        String prefix = "receiveHave";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + RECEIVE_HAVE_LOG + peerID + FOR_PIECE + pieceIndex;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void receiveInterested (int peerID) throws LoggerIOException {
        String prefix = "receiveInterested";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + RECEIVE_INTERESTED_LOG + peerID;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void receiveNotInterested (int peerID) throws LoggerIOException {
        String prefix = "receiveNotInterested";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + RECEIVE_NOT_INTERESTED_LOG + peerID;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void downloadPiece (int peerID, int pieceIndex) throws LoggerIOException {
        String prefix = "downloadPiece";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + DOWNLOAD_PIECE + pieceIndex + " from " + peerID;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void completeDownload () throws LoggerIOException {
        String prefix = "completeDownload";
        try {
            String date = new Date().toString();
            String log = date + PEER_ID + myPeerID + COMPLETE_DOWNLOAD;
            bWriter.write(log);
            bWriter.newLine();
        } catch (IOException ex) {
            String message = prefix + FAIL_IN_WRITING_LOG;
            throw new LoggerIOException(message, ex);
        }
    }

    public static void closeLogger () throws LoggerIOException {
        String prefix = "closeLogger";
        try {
            bWriter.close();
        } catch (IOException ex) {
            String message = prefix + CLOSE_LOGGER_ERROR;
            throw new LoggerIOException(message, ex);
        }
    }

}
