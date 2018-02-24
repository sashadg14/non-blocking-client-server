/**
 * Created by Alex on 15.02.2018.
 */
public class MessagesUtils {

    public boolean isSignInUserMessage(String s){
        if(s.matches("\\/register\\s*user\\s*[a-z,A-Z,\\d]+"))
            return true;
        else return false;
    }

    public boolean isSignInAgentMessage(String s){
        if(s.matches("\\/register\\s*agent\\s*[a-z,A-Z,\\d]+"))
            return true;
        else return false;
    }

    public boolean isSignInMessage(String s){
        if(s.matches("\\/register\\s*(agent|user)\\s*[a-z,A-Z,\\d]+"))
            return true;
        else return false;
    }
    public boolean isExitMessage(String s){
        if(s.matches("\\/exit"))
            return true;
        else return false;
    }
    public boolean isLeaveMessage(String s){
        if(s.matches("\\/leave"))
            return true;
        else return false;
    }

    public String getNameFromMessage(String s){
        return s.replaceAll("\\/register|[\\s]|user|agent","");
    }

    public String getMessageType(String message){
        if (isSignInMessage(message))
            return Constants.MESSAGE_TYPE_REGISTER;
        else if (isExitMessage(message))
            return Constants.MESSAGE_TYPE_EXIT;
        else if(isLeaveMessage(message))
            return Constants.MESSAGE_TYPE_LEAVE;
        else return Constants.MESSAGE_TYPE_SMS;
    }
}
