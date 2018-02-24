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

    private List<Pair<SocketChannel, SocketChannel>> pairUserAgentList = new LinkedList<>();

    public void addNewUser(SocketChannel channel, String name) {
        usersMap.put(channel, name);
        // waitingUsersList.add(channel);
    }

    public void addUserChannelInWaiting(SocketChannel channel) {
        waitingUsersList.add(channel);
    }

    public void addNewAgent(SocketChannel channel, String name) {
        agentsMap.put(channel, name);
        freeArentsList.add(channel);
    }

    public boolean isAutorized(SocketChannel channel, String name) {
        return usersMap.containsKey(channel) || agentsMap.containsKey(channel) || agentsMap.containsValue(name) || usersMap.containsValue(name);
    }

    public String getClientNameByChanel(SocketChannel channel) {
        if (usersMap.containsKey(channel))
            return usersMap.get(channel);
        else if (agentsMap.containsKey(channel))
            return agentsMap.get(channel);
        return "not authorized";
    }

    public boolean doesClientHaveInterlocutor(SocketChannel channel){
        for (Pair<SocketChannel, SocketChannel> pair : pairUserAgentList)
            if (pair.getKey() == channel || pair.getValue() == channel)
                return true;
        return false;
    }

    public void breakChatBetweenUserAndAgent(SocketChannel userChannel) {
        Iterator<Pair<SocketChannel, SocketChannel>> pairIterator = pairUserAgentList.iterator();
        while (pairIterator.hasNext()) {
            Pair<SocketChannel, SocketChannel> pair = pairIterator.next();
            if (pair.getKey() == userChannel) {
                freeArentsList.add(pair.getValue());
                pairIterator.remove();
            }
        }
    }
    public void breakChatBetweenAgentAndUser(SocketChannel agentChannel) {
        Iterator<Pair<SocketChannel, SocketChannel>> pairIterator = pairUserAgentList.iterator();
        while (pairIterator.hasNext()) {
            Pair<SocketChannel, SocketChannel> pair = pairIterator.next();
            if (pair.getValue() == agentChannel) {
                freeArentsList.add(pair.getValue());
                pairIterator.remove();
            }
        }

    }

    public boolean doesItsUserChannel(SocketChannel channel) {
        return usersMap.containsKey(channel);
    }
    public boolean doesItsAgentChannel(SocketChannel channel) {
        return agentsMap.containsKey(channel);
    }

    public SocketChannel getClientInterlocutorChannel(SocketChannel channel) {
        for (Pair<SocketChannel, SocketChannel> pair : pairUserAgentList)
            if (pair.getKey() == channel)
                return pair.getValue();
            else if (pair.getValue() == channel)
                return pair.getKey();
        return null;
    }

    public void removeUserChanelFromBase(SocketChannel userChannel){
        if(waitingUsersList.contains(userChannel))
            waitingUsersList.remove(userChannel);
        if(usersMap.containsKey(userChannel))
            usersMap.remove(userChannel);
    }
    public void removeAgentChanelFromBase(SocketChannel agentChannel){
        if(freeArentsList.contains(agentChannel))
            freeArentsList.remove(agentChannel);
        if(agentsMap.containsKey(agentChannel))
            agentsMap.remove(agentChannel);
    }

    public Pair<SocketChannel, SocketChannel> createNewPairOfUserAndAgent() {
        if (isSomeUsersWait() && isSomeAgentsFree()) {
            //first channel - user, second - agent channel
            Pair<SocketChannel, SocketChannel> pair = new Pair<>(waitingUsersList.remove(0), freeArentsList.remove(0));
            pairUserAgentList.add(pair);
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
