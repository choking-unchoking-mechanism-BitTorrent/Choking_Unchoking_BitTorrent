package analyzer;

import exception.analyzer.AnalyzerException;
import exception.analyzer.ConfigFileNotFoundException;
import exception.analyzer.FileFormatIncorrectException;
import exception.analyzer.FileIOException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PeerInfoAnalyzer {
    public List<PeerInfo> analyze() throws AnalyzerException {
        BufferedReader reader;
        List<PeerInfo> peerInfos = new ArrayList<>();
        try{
            reader = new BufferedReader(new FileReader(Constant.FILE_NAME_PEER_INFO_CFG));

        }catch (FileNotFoundException e){
            throw new ConfigFileNotFoundException(Constant.FILE_NAME_PEER_INFO_CFG);
        }
        String line;
        try{
            line = reader.readLine();
            while (line != null){
                //Divided by space
                String[] components = line.split(Constant.DIVIDER);
                //1.Peer ID 2.Host name 3.Port number 4.Has complete file or not
                if (components.length < 4){
                    throw new FileFormatIncorrectException(
                            Constant.FILE_NAME_PEER_INFO_CFG,
                            FileFormatIncorrectException.MISSING_COMPONENTS);
                }
                else if (components.length > 4){
                    throw new FileFormatIncorrectException(
                            Constant.FILE_NAME_PEER_INFO_CFG,
                            FileFormatIncorrectException.TOO_MANY_COMPONENTS
                    );
                }
                try{
                    String hostName = components[1];
                    int hostID = Integer.parseInt(components[0]);
                    int port = Integer.parseInt(components[2]);
                    int hasCompleteFile = Integer.parseInt(components[3]);
                    peerInfos.add(new PeerInfo(hostName, hostID, port, hasCompleteFile));
                }catch (NumberFormatException e){
                    throw new FileFormatIncorrectException(
                            Constant.FILE_NAME_PEER_INFO_CFG,
                            FileFormatIncorrectException.WRONG_FORMAT);
                }
                line = reader.readLine();
            }
            reader.close();
        }catch (IOException e){
            throw new FileIOException();
        }
        return peerInfos;
    }
}