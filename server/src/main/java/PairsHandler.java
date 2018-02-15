import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Alex on 15.02.2018.
 */
public class PairsHandler {
    Map<SocketChannel,String> usersMap=new LinkedHashMap<>();
    Map<SocketChannel,String> arentsMap=new LinkedHashMap<>();

    public void addNewUser(SocketChannel channel, String name){
        usersMap.put(channel,name);
    }

    public boolean isAutorized(SocketChannel channel){
        if(usersMap.get(channel)!=null||arentsMap.get(channel)!=null)
            return true;
        else return false;
    }
}
