import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.net.InetAddress;
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
                String msg = processRead(key);
                System.out.println(msg);
                if (msg.length() > 0) {
                    ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.write(buffer);
                    buffer.flip();
                }
            }

            setIterator.remove();
        }
    }

    private String processRead(SelectionKey key) throws IOException {
        SocketChannel sChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesCount = sChannel.read(buffer);
        if (bytesCount > 0) {
            //buffer.flip();
            return new String(buffer.array());
        }
        return "NoMessage";
    }

}
