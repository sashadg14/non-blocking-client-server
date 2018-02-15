import javafx.util.Pair;

import java.nio.channels.SocketChannel;
import java.util.*;

/**
 * Created by Alex on 15.02.2018.
 */
public class AllClientsBase {
    private Map<SocketChannel, String> usersMap = new LinkedHashMap<>();
    private List<SocketChannel> waitingUsersList = new LinkedList<>();

    private Map<SocketChannel, String> agentsMap = new LinkedHashMap<>();
    private List<SocketChannel> freeArentsList = new LinkedList<>();

    private List<Pair<SocketChannel, SocketChannel>> pairList = new LinkedList<>();

    public void addNewUser(SocketChannel channel, String name) {
        usersMap.put(channel, name);
        waitingUsersList.add(channel);
    }

    public void addNewAgent(SocketChannel channel, String name) {
        agentsMap.put(channel, name);
        freeArentsList.add(channel);
    }

    public boolean isAutorized(SocketChannel channel, String name) {
        return usersMap.containsKey(channel) || agentsMap.containsKey(channel) || agentsMap.containsValue(name) || usersMap.containsValue(name);
    }

    public String getUserNameByChanel(SocketChannel channel) {
        return usersMap.get(channel);
    }
    public String getAgentNameByChanel(SocketChannel channel) {
        return agentsMap.get(channel);
    }

    public boolean doesClientHasInterlocutor(SocketChannel channel) {
        for (Pair<SocketChannel, SocketChannel> pair : pairList)
            if (pair.getKey() == channel || pair.getValue() == channel)
                return true;
        return false;
    }

    public boolean doesItsUserChannel(SocketChannel channel){
        return usersMap.containsKey(channel);
    }

    public SocketChannel getClientInterlocutorChannel(SocketChannel channel) {
        for (Pair<SocketChannel, SocketChannel> pair : pairList)
            if (pair.getKey() == channel)
                return pair.getValue();
            else if (pair.getValue() == channel)
                return pair.getKey();
        return null;
    }

    public Pair<SocketChannel, SocketChannel> createNewPairOfUserAndAgent(){
        if(isSomeUsersWait()&&isSomeAgentsFree()){
            //first channel - user, second - agent channel
            Pair<SocketChannel,SocketChannel> pair=new Pair<>(waitingUsersList.remove(0),freeArentsList.remove(0));
            pairList.add(pair);
            return pair;
        }
        return null;
    }

    private boolean isSomeUsersWait() {
        return waitingUsersList.size() != 0;
    }

    private boolean isSomeAgentsFree() {
        return freeArentsList.size() != 0;
    }

}
