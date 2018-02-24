import com.sun.javafx.iio.ios.IosDescriptor;
import javafx.util.Pair;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ServerCommunicationTest {
    ServerCommunication serverCommunication;

    @Test
    void handleRegistrationWithErrors() throws IOException {//большой метод, пока что не додумал как получше написать его
        Throwable throwable = null;
        ServerConnection serverConnection = mock(ServerConnection.class);
        AllClientsBase allClientsBase=mock(AllClientsBase.class);
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        SocketChannel socketChannel = mock(SocketChannel.class);

        doThrow(new IOException()).when(serverConnection).sendMessageToClient(any(), anyString());//если хотим отправить сообщение о регистрации клиенту котороый отключился то выкидывается исключение, его и иммитирую
        try {
            serverCommunication.handleRegistration(socketChannel, "/register user alex");
        } catch (IOException e) {
            throwable = e;
        }

        assertTrue(throwable != null);

        throwable = null;
        try {
            serverCommunication.handleRegistration(socketChannel, "/register agent nikolay");
        } catch (IOException e) {
            throwable = e;
        }

        assertTrue(throwable != null);

    }
    @Test
    void handleRegistration() throws IOException {//проверка на то, что бы не было ошибок при обычной регистрации без выбросов исключений при отправке сообщений обратно клиенту
        ServerConnection serverConnection = mock(ServerConnection.class);
        AllClientsBase allClientsBase=new AllClientsBase();
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        SocketChannel socketChannel = mock(SocketChannel.class);
        SocketChannel socketChannel2 = mock(SocketChannel.class);
        serverCommunication.handleRegistration(socketChannel, "/register user alex");
        serverCommunication.handleRegistration(socketChannel2, "/register agent nikolay");
        assertTrue(allClientsBase.isAutorized(socketChannel,"alex"));
        assertTrue(allClientsBase.isAutorized(socketChannel2,"nikolay"));
        //when(allClientsBase.isAutorized(any(),anyString())).thenReturn(true);
    }

    @Test
    void handleMessagesFromAutorizedUser() throws IOException {
        ServerConnection serverConnection = mock(ServerConnection.class);
        AllClientsBase allClientsBase=mock(AllClientsBase.class);
        SocketChannel socketChannel = mock(SocketChannel.class);
        SocketChannel socketChannel2 = mock(SocketChannel.class);

        when(allClientsBase.doesClientHaveInterlocutor(socketChannel)).thenReturn(true);
        when(allClientsBase.getClientInterlocutorChannel(socketChannel)).thenReturn(socketChannel2);
        when(allClientsBase.doesItsUserChannel(socketChannel)).thenReturn(true);
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel,"messaje");
        verify(serverConnection).sendMessageToClient(socketChannel2,"user: messaje");

        when(allClientsBase.doesItsUserChannel(socketChannel)).thenReturn(false);
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel,"messaje");
        verify(serverConnection).sendMessageToClient(socketChannel2,"agent: messaje");

        when(allClientsBase.getClientInterlocutorChannel(socketChannel)).thenReturn(socketChannel2);
        when(allClientsBase.doesItsUserChannel(socketChannel)).thenReturn(true);
        doThrow(new IOException()).when(serverConnection).sendMessageToClient(socketChannel2,"user: messaje");
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel,"messaje");
        //verify(serverCommunication).handlingClientDisconnecting(any());
    }

    @Test
    void handleMessagesFromAutorizedUser2() throws IOException {
        ServerConnection serverConnection = mock(ServerConnection.class);
        AllClientsBase allClientsBase=mock(AllClientsBase.class);
        SocketChannel socketChannel = mock(SocketChannel.class);

        when(allClientsBase.doesClientHaveInterlocutor(socketChannel)).thenReturn(false);
        when(allClientsBase.doesItsUserChannel(socketChannel)).thenReturn(true);
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel,"messaje");
        verify(serverConnection).sendMessageToClient(socketChannel,Constants.WAIT_AGENT);

        when(allClientsBase.doesItsUserChannel(socketChannel)).thenReturn(false);
       // serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel,"messaje");
        verify(serverConnection).sendMessageToClient(socketChannel,Constants.WAIT_USER);
/*
        socketChannel = mock(SocketChannel.class);
        when(allClientsBase.doesItsUserChannel(socketChannel)).thenReturn(true);
        doNothing().when(socketChannel).close();
        doThrow(new IOException()).when(serverConnection).sendMessageToClient(socketChannel,Constants.WAIT_AGENT);
        serverConnection = mock(ServerConnection.class);
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel,"messaje");
        verify(socketChannel).close();*/
    }

    @Test
    void handlingClientDisconnecting() throws IOException {
        ServerConnection serverConnection = mock(ServerConnection.class);
        AllClientsBase allClientsBase=mock(AllClientsBase.class);
        SocketChannel socketChannel1 = mock(SocketChannel.class);
        SocketChannel socketChannel2 = mock(SocketChannel.class);

        when(allClientsBase.doesClientHaveInterlocutor(socketChannel1)).thenReturn(true);
        when(allClientsBase.doesClientHaveInterlocutor(socketChannel2)).thenReturn(true);
        when(allClientsBase.getClientInterlocutorChannel(socketChannel1)).thenReturn(socketChannel2);
        when(allClientsBase.getClientInterlocutorChannel(socketChannel2)).thenReturn(socketChannel1);
        when(allClientsBase.doesItsUserChannel(socketChannel1)).thenReturn(true);
        when(allClientsBase.doesItsAgentChannel(socketChannel2)).thenReturn(true);
        doThrow(new IOException()).when(serverConnection).sendMessageToClient(socketChannel2,"user: messaje");
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel1,"messaje");

        doThrow(new IOException()).when(serverConnection).sendMessageToClient(socketChannel1,"agent: messaje");
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel2,"messaje");


       /* verify(socketChannel2).close();

        when(allClientsBase.doesClientHaveInterlocutor(socketChannel1)).thenReturn(false);
        when(allClientsBase.doesItsUserChannel(socketChannel1)).thenReturn(true);
        doThrow(new IOException()).when(serverConnection).sendMessageToClient(socketChannel2,"user: messaje");
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleMessagesFromAutorizedUser(socketChannel1,"messaje");*/
    }

    @Test
    void tryToCreateNewPair() throws IOException {
        ServerConnection serverConnection = mock(ServerConnection.class);
        AllClientsBase allClientsBase=mock(AllClientsBase.class);
        SocketChannel socketChannel1 = mock(SocketChannel.class);
        SocketChannel socketChannel2 = mock(SocketChannel.class);
        ServerCommunication serverCommunication=new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.tryToCreateNewPair();
        verify(serverConnection,never()).sendMessageToClient(any(),anyString());

        when(allClientsBase.createNewPairOfUserAndAgent()).thenReturn(new Pair<>(socketChannel1,socketChannel2));
        allClientsBase.addNewUser(socketChannel1,"some1");
        allClientsBase.addUserChannelInWaiting(socketChannel1);
        allClientsBase.addNewAgent(socketChannel2,"agent1");
        serverCommunication.tryToCreateNewPair();
        verify(serverConnection,atLeastOnce()).sendMessageToClient(any(),anyString());
    }

    @Test
    void handleClientExit() throws IOException {
        ServerConnection serverConnection = mock(ServerConnection.class);
        AllClientsBase allClientsBase=mock(AllClientsBase.class);
        SocketChannel socketChannel1 = mock(SocketChannel.class);
        SocketChannel socketChannel2 = mock(SocketChannel.class);

        when(allClientsBase.doesClientHaveInterlocutor(socketChannel1)).thenReturn(true);
        when(allClientsBase.doesClientHaveInterlocutor(socketChannel2)).thenReturn(true);
        when(allClientsBase.getClientInterlocutorChannel(socketChannel1)).thenReturn(socketChannel2);
        when(allClientsBase.getClientInterlocutorChannel(socketChannel2)).thenReturn(socketChannel1);
        when(allClientsBase.doesItsUserChannel(socketChannel1)).thenReturn(true);
        when(allClientsBase.doesItsAgentChannel(socketChannel2)).thenReturn(true);
        serverCommunication = new ServerCommunication(allClientsBase,serverConnection);
        serverCommunication.handleClientExit(socketChannel1);
        serverCommunication.handleClientExit(socketChannel2);
    }
}