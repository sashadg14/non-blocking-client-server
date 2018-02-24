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
public class ServerConnection {
    private int port = 19000;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private AllClientsBase allClientsBase = new AllClientsBase();
    private MessagesUtils mUtils = new MessagesUtils();
    private Logger logger = Logger.getRootLogger();
    private ServerCommunication serverCommunication;

    public void createConnection() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        serverCommunication=new ServerCommunication(allClientsBase,this);
    }

    public void listenConnection() throws IOException{
        while (true) {
            if (selector.select() <= 0) {
                continue;
            }
            handleSet(selector.selectedKeys());
            serverCommunication.tryToCreateNewPair();
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
                        serverCommunication.handlingClientDisconnecting((SocketChannel) key.channel());
                        break;
                    }
                    messageHandle(key, message.trim());
                    break;
            }
            keySetIterator.remove();
        }
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
                /*try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                serverCommunication.handleRegistration(clientChannel, message);
                break;
            case Constants.MESSAGE_TYPE_SMS:
                if (!allClientsBase.isAutorized(clientChannel, mUtils.getNameFromMessage(message))) {
                    sendMessageToClient(clientChannel, Constants.ERROR_NEED_REGISTERING);
                } else
                    serverCommunication.handleMessagesFromAutorizedUser(clientChannel, message);
                break;
            case Constants.MESSAGE_TYPE_EXIT:
                serverCommunication.handleClientExit(clientChannel);
                break;
            case Constants.MESSAGE_TYPE_LEAVE:
                if (allClientsBase.doesClientHaveInterlocutor(clientChannel) && allClientsBase.doesItsUserChannel(clientChannel)) {
                    sendMessageToClient(allClientsBase.getClientInterlocutorChannel(clientChannel), "user leave from dialog");
                    allClientsBase.breakChatBetweenUserAndAgent(clientChannel);
                    String agentName = allClientsBase.getClientNameByChanel(allClientsBase.getClientInterlocutorChannel(clientChannel));
                    logger.log(Level.INFO, "user " + allClientsBase.getClientNameByChanel(clientChannel) + " leave from dialog width " + agentName);
                }
                break;
        }
    }



    public void sendMessageToClient(SocketChannel channel, String message) throws IOException {
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
