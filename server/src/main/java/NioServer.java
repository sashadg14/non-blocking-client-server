/**
 * Created by Alex on 14.02.2018.
 */
public class NioServer {

    public static void main(String[] args) throws Exception {
        ServerConnectionHandler serverConnectionHandler =new ServerConnectionHandler();
        serverConnectionHandler.createConnection();
        serverConnectionHandler.listenConnection();
    }

}
