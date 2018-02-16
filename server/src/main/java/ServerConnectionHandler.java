import javafx.util.Pair;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Alex on 14.02.2018.
 */
public class ServerConnectionHandler {
    private int port = 19000;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    AllClientsBase allClientsBase = new AllClientsBase();
    MessagesUtils mUtils = new MessagesUtils();
    UsersSMSCache usersSMSCache = new UsersSMSCache();
    Logger logger = Logger.getLogger(ServerConnectionHandler.class);

    public void createConnection() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void listenConnection() throws IOException {
        while (true) {
            if (selector.select() <= 0) {
                continue;
            }
            handleSet(selector.selectedKeys());
            tryToCreateNewPair();
        }
    }

    public void tryToCreateNewPair() throws IOException {
        Pair<SocketChannel, SocketChannel> pair = allClientsBase.createNewPairOfUserAndAgent();
        if (pair != null) {
            String userName = allClientsBase.getClientNameByChanel(pair.getKey());
            String agentName = allClientsBase.getClientNameByChanel(pair.getValue());
            logger.log(Level.INFO, "Created chat between " + userName + " and " + agentName);
            sendMessageToClient(pair.getKey(), "your agent is " + allClientsBase.getClientNameByChanel(pair.getValue()));
            sendMessageToClient(pair.getValue(), "your user is " + allClientsBase.getClientNameByChanel(pair.getKey()) + "\n");
            sendMessageToClient(pair.getValue(), "user: " + usersSMSCache.removeCachedSMS(pair.getKey()));
        }
    }

    private void handleSet(Set<SelectionKey> set) throws IOException {
        Iterator<SelectionKey> keySetIterator = set.iterator();
        while (keySetIterator.hasNext()) {
            SelectionKey key = keySetIterator.next();
            switch (key.readyOps()) {
                case SelectionKey.OP_ACCEPT:
                    acceptNewConnection();
                    break;
                case SelectionKey.OP_READ:
                    String message = "";
                    try {
                        message = readMessage(key);
                    } catch (IOException e) {
                        logger.log(Level.INFO, "Client " + allClientsBase.getClientNameByChanel((SocketChannel) key.channel()) + " disconnect");
                        handlingClientDisconnecting((SocketChannel) key.channel());
                        break;
                    }
                    messageHandle(key, message.trim());
                    break;
            }
            keySetIterator.remove();
        }
    }

    private void handlingClientDisconnecting(SocketChannel clientChannel) throws IOException {
        if (allClientsBase.doesClientHasInterlocutor(clientChannel))
            if (allClientsBase.doesItsUserChannel(clientChannel)) {
                sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "user disconnected");
                allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                allClientsBase.removeUserChanelFromBase(clientChannel);
            } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "agent disconnected");
                allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
                allClientsBase.removeAgentChanelFromBase(clientChannel);
            }
        clientChannel.close();
    }

    private void acceptNewConnection() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        logger.log(Level.INFO, "Connected new client " + socketChannel.getLocalAddress());
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void messageHandle(SelectionKey key, String message) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        //final String type=;
        switch (mUtils.getMessageType(message.trim())) {
            case Constants.MESSAGE_TYPE_REGISTER:
                handleRegistration(clientChannel, message);
                break;
            case Constants.MESSAGE_TYPE_SMS:
                if (!allClientsBase.isAutorized(clientChannel, mUtils.getNameFromMessage(message))) {
                    sendMessageToClient(clientChannel, Constants.ERROR_NEED_REGISTERING);
                } else
                    handleMessagesFromAutorizedUser(clientChannel, message);
                break;
            case Constants.MESSAGE_TYPE_EXIT:
                handleClientExit(clientChannel);
                break;
            case Constants.MESSAGE_TYPE_LEAVE:
                if (allClientsBase.doesClientHasInterlocutor(clientChannel) && allClientsBase.doesItsUserChannel(clientChannel)) {
                    sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "user leave from dialog");
                    allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                    String agentName = allClientsBase.getClientNameByChanel(allClientsBase.getClientInterlocutorChannel(clientChannel));
                    logger.log(Level.INFO, "user " + allClientsBase.getClientNameByChanel(clientChannel) + " leave from dialog width " + agentName);
                }
                break;
        }
    }

    private void handleClientExit(SocketChannel clientChannel) throws IOException {
        if (allClientsBase.doesClientHasInterlocutor(clientChannel)) {
            if (allClientsBase.doesItsUserChannel(clientChannel)) {
                sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "user exit");
                allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                allClientsBase.removeUserChanelFromBase(clientChannel);
                logger.log(Level.INFO, "user " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
            } else if (allClientsBase.doesItsAgentChannel(clientChannel)) {
                sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "agent exit");
                allClientsBase.breakChatBetweenAgentAndUser(clientChannel);
                allClientsBase.removeAgentChanelFromBase(clientChannel);
                logger.log(Level.INFO, "agent " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
            }
        } else logger.log(Level.INFO, "client " + allClientsBase.getClientNameByChanel(clientChannel) + " exit");
        clientChannel.close();
    }

    private void handleMessagesFromAutorizedUser(SocketChannel userChannel, String message) throws IOException {
        if (allClientsBase.doesClientHasInterlocutor(userChannel)) {
            if (allClientsBase.doesItsUserChannel(userChannel))
                sendMessageToClient(allClientsBase.getClientInterlocutorChannel(userChannel), "user: " + message);
            else sendMessageToClient(allClientsBase.getClientInterlocutorChannel(userChannel), "agent: " + message);
        } else {
            if (allClientsBase.doesItsUserChannel(userChannel)) {
                usersSMSCache.addSMSinCache(userChannel, message);
                allClientsBase.addUserChannelInWaiting(userChannel);
                sendMessageToClient(userChannel, "wait your agent\n");
            } else sendMessageToClient(userChannel, "you have't user\n");
        }
    }

    private void handleRegistration(SocketChannel userChannel, String message) throws IOException {
        String name = mUtils.getNameFromMessage(message);
        if (allClientsBase.isAutorized(userChannel, name)) {
            sendMessageToClient(userChannel, Constants.ERROR_ALREADY_REGISTRED);
            return;
        }
        if (mUtils.isSignInUserMessage(message)) {
            allClientsBase.addNewUser(userChannel, name);
            sendMessageToClient(userChannel, Constants.SUCCESS_REGISTRED);
            logger.log(Level.INFO, "Registered user " + name);
            //System.out.println("user");
        } else if (mUtils.isSignInAgentMessage(message)) {
            allClientsBase.addNewAgent(userChannel, name);
            sendMessageToClient(userChannel, Constants.SUCCESS_REGISTRED + "\n");
            logger.log(Level.INFO, "Registered agent " + name);
            // System.out.println("agent");
        }
    }

    private void sendMessageToClient(SocketChannel channel, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        channel.write(buffer);
        buffer.flip();
    }

    private String readMessage(SelectionKey key) throws IOException {
        SocketChannel sChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesCount = sChannel.read(buffer);
        if (bytesCount > 0) {
            buffer.flip();
            return new String(buffer.array());
        }
        return "";
    }

}
