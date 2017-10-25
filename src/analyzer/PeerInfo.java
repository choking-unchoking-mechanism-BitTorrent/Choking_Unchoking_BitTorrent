package analyzer;

import lombok.Data;

@Data
public class PeerInfo {
    final String hostName;
    final int hostID;
    final int port;
    final int hasCompleteFile;
}
