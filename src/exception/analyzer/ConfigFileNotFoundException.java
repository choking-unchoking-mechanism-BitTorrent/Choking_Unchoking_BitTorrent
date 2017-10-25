package exception.analyzer;

public class ConfigFileNotFoundException extends AnalyzerException{
    private String fileName;
    public ConfigFileNotFoundException(String fileName){
        this.fileName = fileName;
    }
    @Override
    public String toString(){
        return "Config file "+ fileName +" not found in executing directory!\n";
    }
}