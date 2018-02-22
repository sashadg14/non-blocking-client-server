import javax.swing.*;
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
import java.util.Scanner;
import java.util.Set;

/**
 * Created by Alex on 14.02.2018.
 */

public class ClientConnectionHandler {
    private Selector selector;
    private SocketChannel channel;
    private volatile boolean isActive=true;
    private Scanner scanner=new Scanner(System.in);
    private Thread thread = new Thread(() -> {
        while (scanner.hasNext()&&isActive){
            String message = scanner.nextLine();
            ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
            try {
                channel.write(buffer);
                if (message.matches("\\/exit"))
                    isActive=false;
            } catch (IOException e) {
               scanner.close();
                break;
            }
        }
    });

    public void closeThread(){
        isActive=false;
        scanner.close();
    }

    private String readString() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(4024);
        channel.read(buffer);
        buffer.flip();
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer charBuffer = decoder.decode(buffer);
        String msg = charBuffer.toString();
        return msg;
    }

    private boolean processReadySet(Set readySet) {
        Iterator iterator = readySet.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = (SelectionKey)
                    iterator.next();
            if (key.isReadable()) {
                String msg = null;
                try {
                    msg = readString();
                } catch (Exception e){
                    System.out.println("Server broken down");
                    return true;
                }
                if (msg.equalsIgnoreCase(""))
                    return true;
                System.out.println("-> " + msg);
            }
            iterator.remove();
        }
        return false;
    }

    public void listenConnection() throws Exception {
        thread.setDaemon(true);
        thread.start();
        while (true) {
            if (selector.select() > 0) {
                boolean isDone = processReadySet(selector.selectedKeys());
                if (isDone) {
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
