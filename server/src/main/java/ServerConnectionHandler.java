import javafx.util.Pair;

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
    UsersSMSCache usersSMSCache=new UsersSMSCache();

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
        Pair<SocketChannel,SocketChannel> pair=allClientsBase.createNewPairOfUserAndAgent();
        if(pair!=null){
            sendMessageToClient(pair.getKey(),"your agent is "+allClientsBase.getAgentNameByChanel(pair.getValue()));
            sendMessageToClient(pair.getValue(),"your user is "+allClientsBase.getUserNameByChanel(pair.getKey())+"\n");
            sendMessageToClient(pair.getValue(),"user: "+usersSMSCache.removeCachedSMS(pair.getKey()));
        }
    }

    private void handleSet(Set<SelectionKey> set) throws IOException {
        Iterator<SelectionKey> setIterator = set.iterator();
        while (setIterator.hasNext()) {
            SelectionKey key = setIterator.next();
            switch (key.readyOps()) {
                case SelectionKey.OP_ACCEPT:
                    acceptNewConnection();
                    break;
                case SelectionKey.OP_READ:
                    String message = readMessage(key);
                    messageHandle(key, message.trim());
                    break;
            }
            setIterator.remove();
        }
    }

    private void acceptNewConnection() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void messageHandle(SelectionKey key, String message) throws IOException {
        SocketChannel userChannel= (SocketChannel) key.channel();
        switch (mUtils.getMessageType(message)) {
            case Constants.MESSAGE_TYPE_REGISTER:
                handleRegistration(key,message);
                break;
            case Constants.MESSAGE_TYPE_SMS:
                if (!allClientsBase.isAutorized(userChannel, mUtils.getNameFromMessage(message))) {
                    sendMessageToClient(userChannel, Constants.ERROR_NEED_REGISTERING);
                } else {
                   // System.out.println(allClientsBase.getUserNameByChanel((SocketChannel) key.channel()));
                    handleMessagesFromAutorizedUser(userChannel,message);
                }
                break;
            case Constants.MESSAGE_TYPE_EXIT:
                //TODO: отключение юзера
               // key.channel().close();
                break;
            case Constants.MESSAGE_TYPE_LEAVE:
                System.out.println("leave");
                if (allClientsBase.doesItsUserChannel(userChannel)) {
                    sendMessageToClient(allClientsBase.getClientInterlocutorChannel(userChannel), "user leave from dialog");
                    allClientsBase.breakConnBetweenUserAndAgent(userChannel);
                }
                break;
        }
    }

    private void handleMessagesFromAutorizedUser(SocketChannel userChannel,String message) throws IOException {
        if(allClientsBase.doesClientHasInterlocutor(userChannel)) {
            if (allClientsBase.doesItsUserChannel(userChannel))
                sendMessageToClient(allClientsBase.getClientInterlocutorChannel(userChannel),"user: "+message);
            else sendMessageToClient(allClientsBase.getClientInterlocutorChannel(userChannel),"agent: "+message);
        } else {
            if (allClientsBase.doesItsUserChannel(userChannel)) {
                usersSMSCache.addSMSinCache(userChannel,message);
                allClientsBase.addUserChannelInWaiting(userChannel);
                sendMessageToClient(userChannel,"wait your agent\n");
            }
            else sendMessageToClient(userChannel,"you have't user\n");
        }
    }
    
    private void handleRegistration(SelectionKey key,String message) throws IOException {
        String name = mUtils.getNameFromMessage(message);
        if (allClientsBase.isAutorized((SocketChannel) key.channel(), name)) {
            sendMessageToClient((SocketChannel) key.channel(), Constants.ERROR_ALREADY_REGISTRED);
            return;
        }
        if (mUtils.isSignInUserMessage(message)) {
            allClientsBase.addNewUser((SocketChannel) key.channel(), name);
            sendMessageToClient((SocketChannel) key.channel(), Constants.SUCCESS_REGISTRED);
            //System.out.println("user");
        } else if (mUtils.isSignInAgentMessage(message)) {
            allClientsBase.addNewAgent((SocketChannel) key.channel(), name);
            sendMessageToClient((SocketChannel) key.channel(), Constants.SUCCESS_REGISTRED+"\n");
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
