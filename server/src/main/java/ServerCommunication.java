import javafx.util.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class ServerCommunication {
    private AllClientsBase allClientsBase;
    private MessagesUtils mUtils = new MessagesUtils();
    private UsersSMSCache usersSMSCache = new UsersSMSCache();
    private Logger logger = Logger.getRootLogger();
    private ServerConnection serverConnection;

    public ServerCommunication(AllClientsBase allClientsBase, ServerConnection serverConnection) {
        this.allClientsBase = allClientsBase;
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

    public void handleMessagesFromAutorizedUser(SocketChannel userChannel, String message) {
        if (allClientsBase.doesClientHaveInterlocutor(userChannel)) {
            try {
                sendMessageToInterlocutorOf(userChannel, message);
            } catch (IOException e) {
                logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel(allClientsBase.getClientInterlocutorChannel(userChannel)) + " disconnect");
                handlingClientDisconnecting(allClientsBase.getClientInterlocutorChannel(userChannel));
            }
        } else {
            try {
                sendMessageBackToClient(userChannel, message);
            } catch (IOException e) {
                logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel(userChannel) + " disconnect");
                handlingClientDisconnecting(userChannel);
            }
        }
    }

    private void sendMessageToInterlocutorOf(SocketChannel clientChannel, String message) throws IOException {
        if (allClientsBase.doesItsUserChannel(clientChannel)) {
            serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "user: " + message);
        } else
            serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "agent: " + message);
    }

    private void sendMessageBackToClient(SocketChannel clientChannel, String message) throws IOException {
        if (allClientsBase.doesItsUserChannel(clientChannel)) {
            usersSMSCache.addSMSinCache(clientChannel, message);
            allClientsBase.addUserChannelInWaiting(clientChannel);
            serverConnection.sendMessageToClient(clientChannel, Constants.WAIT_AGENT);
        } else serverConnection.sendMessageToClient(clientChannel, Constants.WAIT_USER);
    }

    public void handlingClientDisconnecting(SocketChannel clientChannel) {
        if (allClientsBase.doesClientHaveInterlocutor(clientChannel)) {
            if (allClientsBase.doesItsUserChannel(clientChannel)) {
                breakUserChannelConn(clientChannel);
            } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                breakAgentChannelConn(clientChannel);
            }
        } else handleSingleClientDisconnecting(clientChannel);
        try {
            clientChannel.close();
        } catch (Exception ignored) {
        }
    }

    private void breakUserChannelConn(SocketChannel clientChannel) {
        SocketChannel channel = allClientsBase.getClientInterlocutorChannel(clientChannel);
        allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
        allClientsBase.removeUserChanelFromBase(clientChannel);
        try {
            serverConnection.sendMessageToClient(channel, "user disconnected");
        } catch (IOException e) {
            handlingClientDisconnecting(channel);
        }
    }

    private void breakAgentChannelConn(SocketChannel clientChannel) {
        SocketChannel channel = allClientsBase.getClientInterlocutorChannel(clientChannel);
        allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
        allClientsBase.removeAgentChanelFromBase(clientChannel);
        try {
            serverConnection.sendMessageToClient(channel, "agent disconnected");
        } catch (IOException e) {
            handlingClientDisconnecting(channel);
        }
    }

    private void handleSingleClientDisconnecting(SocketChannel clientChannel) {
        if (allClientsBase.doesItsUserChannel(clientChannel))
            allClientsBase.removeUserChanelFromBase(clientChannel);
        else if (allClientsBase.doesItsAgentChannel(clientChannel))
            allClientsBase.removeAgentChanelFromBase(clientChannel);
    }

    public void tryToCreateNewPair() {
        Pair<SocketChannel, SocketChannel> pair = allClientsBase.createNewPairOfUserAndAgent();
        if (pair != null) {
            String userName = allClientsBase.getClientNameByChanel(pair.getKey());
            String agentName = allClientsBase.getClientNameByChanel(pair.getValue());
            logger.log(Level.INFO, "Created chat between " + userName + " and " + agentName);
            try {
                serverConnection.sendMessageToClient(pair.getKey(), "your agent is " + allClientsBase.getClientNameByChanel(pair.getValue()));
            } catch (IOException e) {
                handlingClientDisconnecting(pair.getKey());
            }
            try {
                serverConnection.sendMessageToClient(pair.getValue(), "your user is " + allClientsBase.getClientNameByChanel(pair.getKey()) + "\n");
                serverConnection.sendMessageToClient(pair.getValue(), "user: " + usersSMSCache.removeCachedSMS(pair.getKey()));
            } catch (IOException e){
                handlingClientDisconnecting(pair.getValue());
            }
        }
    }


    public void handleClientExit(SocketChannel clientChannel) {
        try {
            clientChannel.close();
        } catch (Exception ignored) {
        }
        if (allClientsBase.doesClientHaveInterlocutor(clientChannel)) {
            try {
                if (allClientsBase.doesItsUserChannel(clientChannel)) {
                    logger.log(Level.INFO, "user " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
                    serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "user exit");
                    allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                    allClientsBase.removeUserChanelFromBase(clientChannel);
                } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                    logger.log(Level.INFO, "agent " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
                    serverConnection.sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "agent exit");
                    allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
                    allClientsBase.removeAgentChanelFromBase(clientChannel);
                }
            } catch (IOException e) {
                handlingClientDisconnecting(allClientsBase.getClientInterlocutorChannel(clientChannel));
            }
        } else logger.log(Level.INFO, "client " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
    }
}
