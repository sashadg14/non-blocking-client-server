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
public class NioClient {

    public static void main(String[] args){
      ClientConectionHandler clientConectionHandler=new ClientConectionHandler();
      try {
          clientConectionHandler.createConnection(args[0], Integer.parseInt(args[1]));
      } catch (IOException e){
          System.out.println("Can't connect to "+args[0]);
          return;
      }
      try {
            clientConectionHandler.listenConnection();
        } catch (Exception e) {
          System.out.println("Error in client work");
        }
    }
}
