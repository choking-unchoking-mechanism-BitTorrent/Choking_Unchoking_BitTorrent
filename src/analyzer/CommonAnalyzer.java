package analyzer;

import exception.analyzer.AnalyzerException;
import exception.analyzer.ConfigFileNotFoundException;
import exception.analyzer.FileFormatIncorrectException;
import exception.analyzer.FileIOException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class CommonAnalyzer {

    //All attributes here
    private final static String[] INT_TYPE_ATTRIBUTES = {
            Constant.STRING_NUMBER_OF_PREFER_NEIGHBOURS,
            Constant.STRING_UNCHOKING_INTERVAL,
            Constant.STRING_OPTIMISTIC_UNCHOKING_INTERVAL,
            Constant.STRING_FILE_SIZE,
            Constant.STRING_PIECE_SIZE
    };
    private final static String[] STRING_TYPE_ATTRIBUTES = {
            Constant.STRING_FILE_NAME
    };
    private final static int INT = 0;
    private final static int STRING = 1;

    private HashMap<String, Object> hashMap = new HashMap<>();

    public HashMap<String, Object> analyze() throws AnalyzerException{
        BufferedReader reader;
        try{
            reader = new BufferedReader(new FileReader(Constant.FILE_NAME_COMMON_CFG));

        }catch (FileNotFoundException e){
            throw new ConfigFileNotFoundException(Constant.FILE_NAME_COMMON_CFG);
        }
        String line;
        try{
            line = reader.readLine();
            while (line != null){
                //Divided by space
                String[] components = line.split(Constant.DIVIDER);
                if (components.length > 2){
                    throw new FileFormatIncorrectException(
                            Constant.FILE_NAME_COMMON_CFG,
                            FileFormatIncorrectException.TOO_MANY_COMPONENTS
                    );
                }
                else if (components.length < 2){
                    throw new FileFormatIncorrectException(
                            Constant.FILE_NAME_COMMON_CFG,
                            FileFormatIncorrectException.MISSING_COMPONENTS
                    );
                }
                String key = components[0];
                String value = components[1];
                checkComponent(INT_TYPE_ATTRIBUTES, key, value, INT);
                checkComponent(STRING_TYPE_ATTRIBUTES, key, value, STRING);

                line = reader.readLine();
            }
            reader.close();
        }catch (IOException e){
            throw new FileIOException();
        }
        if (hashMap.size() != INT_TYPE_ATTRIBUTES.length + STRING_TYPE_ATTRIBUTES.length){
            throw new FileFormatIncorrectException(
                    Constant.FILE_NAME_COMMON_CFG,
                    FileFormatIncorrectException.MISSING_COMPONENTS
            );
        }
        return hashMap;
    }

    private void checkComponent(String[] attributes, String key,
                                String value, int attributeType)
            throws  FileFormatIncorrectException{
        for (String attributeName : attributes){
            if (key.equals(attributeName)){
                //Check if duplicated.
                if (hashMap.containsKey(key)){
                    throw new FileFormatIncorrectException(
                            Constant.FILE_NAME_COMMON_CFG,
                            FileFormatIncorrectException.DUPLICATE_COMPONENTS
                    );
                }
                try{
                    if (attributeType == INT){
                        int intValue = Integer.parseInt(value);
                        hashMap.put(key, intValue);
                    }
                    else
                        hashMap.put(key, value);
                }catch (NumberFormatException e){
                    throw new FileFormatIncorrectException(
                            Constant.FILE_NAME_COMMON_CFG,
                            FileFormatIncorrectException.WRONG_FORMAT
                    );
                }
            }
        }

    }
}