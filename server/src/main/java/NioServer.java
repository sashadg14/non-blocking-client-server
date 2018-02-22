/**
 * Created by Alex on 14.02.2018.
 */
public class NioServer {

    public static void main(String[] args) throws Exception {
        ServerConnection serverConnection =new ServerConnection();
        serverConnection.createConnection();
        System.out.println("Server started\n");
        serverConnection.listenConnection();
    }
}
