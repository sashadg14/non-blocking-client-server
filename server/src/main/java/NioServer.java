import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Created by Alex on 14.02.2018.
 */
public class NioServer {

    public static void main(String[] args){
        ServerConnection serverConnection =new ServerConnection();
        try {
            serverConnection.createConnection();
        } catch (IOException e) {
            Logger.getRootLogger().log(Level.TRACE,e);
            return;
        }
        System.out.println("Server started\n");
        try {
            serverConnection.listenConnection();
        } catch (IOException e) {
            Logger.getRootLogger().log(Level.TRACE,e);
            System.out.println("Error in server work");
        }
    }
}
