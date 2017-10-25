package exception.logger;

/**
 * Created by qiaochu on 10/25/17.
 */
public class LoggerIOException extends Exception {
    public LoggerIOException (String msg, Throwable cause){
        super(msg, cause);
    }
    public LoggerIOException() {
        super();
    }
    public LoggerIOException(String msg){
        super(msg);
    }
    public LoggerIOException(Throwable cause){
        super(cause);
    }
}
