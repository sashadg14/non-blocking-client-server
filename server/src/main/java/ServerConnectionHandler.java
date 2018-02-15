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
    PairsHandler pairsHandler=new PairsHandler();
    public void createConnection() throws IOException {
        selector = Selector.open();
        serverSocketChannel= ServerSocketChannel.open();
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
            if (key.isAcceptable()) {
                SocketChannel socketChannel =serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
               // (socketChannel).write(ByteBuffer.wrap("qewrreqwrqew".getBytes()));
            } else
            if (key.isReadable()) {
                String message = readMessage(key);
                System.out.println(message);
                if(isSignInUserMessage(message.trim()))
                    if(!pairsHandler.isAutorized((SocketChannel) key.channel())) {
                        pairsHandler.addNewUser((SocketChannel) key.channel(), getNameFromMessage(message));
                        sendMessageToClient(key,"SUCCESS:YOU REGISTERED");
                }
                else {
                        sendMessageToClient(key,"ERROR:YOU ALREADY REGISTERED");
                    }
                //System.out.println("client alredy aut");
                /*if (message.length() > 0) {
                    ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.write(buffer);
                    buffer.flip();
                }*/
            }
            setIterator.remove();
        }
    }

    private void sendMessageToClient(SelectionKey key,String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        SocketChannel socketChannel = (SocketChannel) key.channel();
        socketChannel.write(buffer);
        buffer.flip();
    }

    private boolean isSignInUserMessage(String s){
        if(s.equalsIgnoreCase("/register user Alex"))
            return true;
        else return false;
    }

    private boolean isSignInAgentMessage(String s){
        if(s.equals("/register user Alex"))
            return true;
        else return false;
    }

    private String getNameFromMessage(String s){
        return s.replaceAll("\\/register|[\\s]|user|agent","");
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
