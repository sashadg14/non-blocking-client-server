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
    PairsHandler pairsHandler = new PairsHandler();
    MessagesUtils mUtils = new MessagesUtils();

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
        switch (mUtils.getMessageType(message)) {
            case Constants.MESSAGE_TYPE_REGISTER:
                if (mUtils.isSignInUserMessage(message))
                    if (!pairsHandler.isAutorized((SocketChannel) key.channel())) {
                        pairsHandler.addNewUser((SocketChannel) key.channel(), mUtils.getNameFromMessage(message));
                        sendMessageToClient(key, Constants.SUCCESS_REGISTRED);
                    } else {
                        sendMessageToClient(key, Constants.ERROR_ALREADY_REGISTRED);
                    }
                break;
            case Constants.MESSAGE_TYPE_SMS:
                if (!pairsHandler.isAutorized((SocketChannel) key.channel())) {
                    sendMessageToClient(key, Constants.ERROR_NEED_REGISTERING);
                } else {
                    //TODO: отправка сообщений другому юзеру
                }
                break;
            case Constants.MESSAGE_TYPE_EXIT:
                key.channel().close();
                break;
            case Constants.MESSAGE_TYPE_LEAVE:
                //TODO: открепление юзера
                break;
        }
    }

    private void sendMessageToClient(SelectionKey key, String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.write(buffer);
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
