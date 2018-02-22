import javafx.util.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ServerCommunication {
    private AllClientsBase allClientsBase = new AllClientsBase();
    private MessagesUtils mUtils = new MessagesUtils();
    private UsersSMSCache usersSMSCache = new UsersSMSCache();
    private Logger logger = Logger.getRootLogger();
    private ServerConnection serverConnection;

    public ServerCommunication(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
    }

    public void handleRegistration(SocketChannel userChannel, String message) throws IOException {
        String name = mUtils.getNameFromMessage(message);
        if (allClientsBase.isAutorized(userChannel, name)) {
            serverConnection.sendMessageToClient(userChannel, Constants.ERROR_ALREADY_REGISTRED);
            return;
        }
        if (mUtils.isSignInUserMessage(message)) {
            serverConnection.sendMessageToClient(userChannel, Constants.SUCCESS_REGISTRED);
            allClientsBase.addNewUser(userChannel, name);
            logger.log(Level.INFO, "Registered user " + name);
            //System.out.println("user");
        } else if (mUtils.isSignInAgentMessage(message)) {
            serverConnection.sendMessageToClient(userChannel, Constants.SUCCESS_REGISTRED + "\n");
            allClientsBase.addNewAgent(userChannel, name);
            logger.log(Level.INFO, "Registered agent " + name);
            // System.out.println("agent");
        }
    }

    public void handleMessagesFromAutorizedUser(SocketChannel userChannel, String message) throws IOException {
        if (allClientsBase.doesClientHaveInterlocutor(userChannel)) {
            if (allClientsBase.doesItsUserChannel(userChannel))
                serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(userChannel), "user: " + message);
            else serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(userChannel), "agent: " + message);
        } else {
            if (allClientsBase.doesItsUserChannel(userChannel)) {
                usersSMSCache.addSMSinCache(userChannel, message);
                allClientsBase.addUserChannelInWaiting(userChannel);
                serverConnection.sendMessageToClient(userChannel, "wait your agent\n");
            } else serverConnection.sendMessageToClient(userChannel, "you have't user\n");
        }
    }

    public void handlingClientDisconnecting(SocketChannel clientChannel) throws IOException {
        if (allClientsBase.doesClientHaveInterlocutor(clientChannel))
            if (allClientsBase.doesItsUserChannel(clientChannel)) {
                serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "user disconnected");
                allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                allClientsBase.removeUserChanelFromBase(clientChannel);
            } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "agent disconnected");
                allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
                allClientsBase.removeAgentChanelFromBase(clientChannel);
            }
        clientChannel.close();
    }

    public void tryToCreateNewPair() throws IOException {
        Pair<SocketChannel, SocketChannel> pair = allClientsBase.createNewPairOfUserAndAgent();
        if (pair != null) {
            String userName = allClientsBase.getClientNameByChanel(pair.getKey());
            String agentName = allClientsBase.getClientNameByChanel(pair.getValue());
            logger.log(Level.INFO, "Created chat between " + userName + " and " + agentName);
            serverConnection.sendMessageToClient(pair.getKey(), "your agent is " + allClientsBase.getClientNameByChanel(pair.getValue()));
            serverConnection.sendMessageToClient(pair.getValue(), "your user is " + allClientsBase.getClientNameByChanel(pair.getKey()) + "\n");
            serverConnection.sendMessageToClient(pair.getValue(), "user: " + usersSMSCache.removeCachedSMS(pair.getKey()));
        }
    }


    public void handleClientExit(SocketChannel clientChannel) throws IOException {
        clientChannel.close();
        if (allClientsBase.doesClientHaveInterlocutor(clientChannel)) {
            if (allClientsBase.doesItsUserChannel(clientChannel)) {
                serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "user exit");
                allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                allClientsBase.removeUserChanelFromBase(clientChannel);
                logger.log(Level.INFO, "user " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
            } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "agent exit");
                allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
                allClientsBase.removeAgentChanelFromBase(clientChannel);
                logger.log(Level.INFO, "agent " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
            }
        } else logger.log(Level.INFO, "client " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
    }

}
