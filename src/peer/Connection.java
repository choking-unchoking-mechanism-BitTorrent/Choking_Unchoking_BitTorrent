package peer;

import analyzer.PeerInfo;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection extends Thread {
    private Peer targetPeer;

    public Connection(Peer peer) {
        this.targetPeer = peer;
    }

    @Override
    public void run() {
        try {
            Socket requestSocket = new Socket(targetPeer.getHost(), targetPeer.getPort());
            ObjectOutputStream out = new ObjectOutputStream(requestSocket.getOutputStream());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
