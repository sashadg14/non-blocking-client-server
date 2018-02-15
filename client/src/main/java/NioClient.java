import java.io.IOException;

/**
 * Created by Alex on 14.02.2018.
 */
public class NioClient {

    public static void main(String[] args) {
        ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler();
        try {
            clientConnectionHandler.createConnection(args[0], Integer.parseInt(args[1]));
        } catch (IOException e) {
            System.out.println("Can't connect to " + args[0]);
            return;
        }
        try {
            clientConnectionHandler.listenConnection();
        } catch (Exception e) {
            System.out.println("Error in client work");
            clientConnectionHandler.closeThread();
        }
    }
}
