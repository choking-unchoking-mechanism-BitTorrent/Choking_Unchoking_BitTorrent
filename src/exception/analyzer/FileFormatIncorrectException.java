package exception.analyzer;

public class FileFormatIncorrectException extends AnalyzerException{

    public final static int MISSING_COMPONENTS = 0;
    public final static int TOO_MANY_COMPONENTS = 1;
    public final static int DUPLICATE_COMPONENTS = 2;
    public final static int NONEXISTENT_COMPONENT = 3;
    public final static int WRONG_FORMAT = 4;

    private final static String STRING_ERROR_MISSING_COMPONENTS = "Missing components in file ";
    private final static String STRING_ERROR_TOO_MANY_COMPONENTS = "Too many components in file ";
    private final static String STRING_ERROR_DUPLICATE_COMPONENTS = "Duplicate components in file ";
    private final static String STRING_ERROR_NONEXISTENT_COMPONENTS = "Nonexistent components in file ";
    private final static String STRING_ERROR_WRONG_FORMAT = "Value type wrong in file ";
    private final static String STRING_UNKNOWN_ERROR = "Unknown error in ";
    private final static String STRING_END_OF_ERROR_MESSAGE = ".\n";

    private String errorMessage;

    public FileFormatIncorrectException(String fileName, int errorCode){
        switch (errorCode){
            case MISSING_COMPONENTS:
                errorMessage = STRING_ERROR_MISSING_COMPONENTS;
                break;
            case TOO_MANY_COMPONENTS:
                errorMessage = STRING_ERROR_TOO_MANY_COMPONENTS;
                break;
            case DUPLICATE_COMPONENTS:
                errorMessage = STRING_ERROR_DUPLICATE_COMPONENTS;
                break;
            case NONEXISTENT_COMPONENT:
                errorMessage = STRING_ERROR_NONEXISTENT_COMPONENTS;
                break;
            case WRONG_FORMAT:
                errorMessage = STRING_ERROR_WRONG_FORMAT;
                break;
            default:
                errorMessage = STRING_UNKNOWN_ERROR;
        }
        errorMessage += fileName + STRING_END_OF_ERROR_MESSAGE;
    }
    @Override
    public String toString(){
        return errorMessage;
    }
}
