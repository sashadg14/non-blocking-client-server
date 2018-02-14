import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Alex on 14.02.2018.
 */
public class ClientConectionHandler {
    private Selector selector;
    SocketChannel channel;
    Thread thread = new Thread(() -> {
        BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String message = null/* "q1234567"*/;
            try {
                message = userInputReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            try {
                channel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });

    public String readString() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(4024);
        channel.read(buffer);
        buffer.flip();
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = decoder.decode(buffer);
        String msg = charBuffer.toString();
        return msg;
    }

    public boolean processReadySet(Set readySet) throws Exception {
        Iterator iterator = readySet.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = (SelectionKey)
                    iterator.next();
            if (key.isReadable()) {
                String msg = readString();
                System.out.println("-> " + msg);
            }
            iterator.remove();
        }
        return false;
    }

    public void listenConnection() throws Exception {
        thread.start();
        while (true) {
            if (selector.select() > 0) {
                boolean doneStatus = processReadySet(selector.selectedKeys());
                if (doneStatus) {
                    break;
                }
            }
        }
        channel.close();
    }

    public void createConnection(String ip, int port) throws IOException {
        InetAddress inetAddress = InetAddress.getByName(ip);
        channel = SocketChannel.open(new InetSocketAddress(inetAddress, port));
        channel.configureBlocking(false);
        selector = Selector.open();
        int operations = SelectionKey.OP_READ;
        channel.register(selector, operations);
    }

}
