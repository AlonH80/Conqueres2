import generated.*;

public class InvalidXMLException extends Exception{
    String reason;

    public InvalidXMLException(){
        reason="Unknown";
    }

    public InvalidXMLException(String reason){
        this.reason=reason;
    }

    @Override
    public String toString(){
        return reason;
    }

}
