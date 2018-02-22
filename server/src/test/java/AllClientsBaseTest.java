import org.junit.jupiter.api.Test;

import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AllClientsBaseTest {
    private SocketChannel channel1=mock(SocketChannel.class);
    private SocketChannel channel2=mock(SocketChannel.class);
    private SocketChannel channel3=mock(SocketChannel.class);
    AllClientsBase allClientsBase;

    @Test
    void isAutorized() {
        allClientsBase = new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");

        assertTrue(allClientsBase.isAutorized(channel1,"agent"));
        assertTrue(allClientsBase.isAutorized(channel2,"user"));
        assertTrue(allClientsBase.isAutorized(channel3,"user"));
        assertTrue(allClientsBase.isAutorized(channel1,"user"));
        assertTrue(allClientsBase.isAutorized(channel1,"agent"));
        assertFalse(allClientsBase.isAutorized(channel3,"nikolay"));

    }

    @Test
    void getClientNameByChanel() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");

        assertEquals(allClientsBase.getClientNameByChanel(channel1),"agent");
        assertEquals(allClientsBase.getClientNameByChanel(channel2),"user");
        assertNotEquals(allClientsBase.getClientNameByChanel(channel3),"user");
        assertNotEquals(allClientsBase.getClientNameByChanel(channel3),"agent");
        assertNotEquals(allClientsBase.getClientNameByChanel(channel1),"user");
    }

    @Test
    void doesClientHaveInterlocutor() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        allClientsBase.addUserChannelInWaiting(channel2);
        allClientsBase.createNewPairOfUserAndAgent();
        boolean have=allClientsBase.doesClientHaveInterlocutor(channel3);
        assertFalse(have);
        have=allClientsBase.doesClientHaveInterlocutor(channel1);
        assertTrue(have);
        have=allClientsBase.doesClientHaveInterlocutor(channel2);
        assertTrue(have);
    }

    @Test
    void breakChatBetweenUserAndAgent() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        allClientsBase.addUserChannelInWaiting(channel2);
        allClientsBase.createNewPairOfUserAndAgent();

        allClientsBase.breakChatBetweenUserAndAgent(channel1);//если в метод передать канал агнта а не клиента
        boolean have=allClientsBase.doesClientHaveInterlocutor(channel1);
        assertTrue(have);
        have=allClientsBase.doesClientHaveInterlocutor(channel2);
        assertTrue(have);

        allClientsBase.breakChatBetweenUserAndAgent(channel2);
        have=allClientsBase.doesClientHaveInterlocutor(channel1);
        assertFalse(have);
        have=allClientsBase.doesClientHaveInterlocutor(channel2);
        assertFalse(have);
    }

    @Test
    void breakChatBetweenAgentAndUser() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        allClientsBase.addUserChannelInWaiting(channel2);
        allClientsBase.createNewPairOfUserAndAgent();

        allClientsBase.breakChatBetweenAgentAndUser(channel2);//если в метод передать канал юзера а не агента
        boolean have=allClientsBase.doesClientHaveInterlocutor(channel1);
        assertTrue(have);
        have=allClientsBase.doesClientHaveInterlocutor(channel2);
        assertTrue(have);

        allClientsBase.breakChatBetweenAgentAndUser(channel1);
        have=allClientsBase.doesClientHaveInterlocutor(channel1);
        assertFalse(have);
        have=allClientsBase.doesClientHaveInterlocutor(channel2);
        assertFalse(have);
    }

    @Test
    void doesItsUserChannel() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        assertFalse(allClientsBase.doesItsUserChannel(channel1));
        assertTrue(allClientsBase.doesItsUserChannel(channel2));
        assertFalse(allClientsBase.doesItsUserChannel(channel3));
    }

    @Test
    void doesItsAgentChannel() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        assertFalse(allClientsBase.doesItsAgentChannel(channel2));
        assertTrue(allClientsBase.doesItsAgentChannel(channel1));
        assertFalse(allClientsBase.doesItsAgentChannel(channel3));
    }

    @Test
    void getClientInterlocutorChannel() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        allClientsBase.addUserChannelInWaiting(channel2);
        allClientsBase.createNewPairOfUserAndAgent();

        assertEquals(allClientsBase.getClientInterlocutorChannel(channel1),channel2);
        assertEquals(allClientsBase.getClientInterlocutorChannel(channel2),channel1);
        assertNotEquals(allClientsBase.getClientInterlocutorChannel(channel2),channel3);
        assertNotEquals(allClientsBase.getClientInterlocutorChannel(channel3),channel1);
    }

    @Test
    void removeUserChanelFromBase() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        allClientsBase.addUserChannelInWaiting(channel2);
        assertNotNull(allClientsBase.createNewPairOfUserAndAgent());
        allClientsBase.addUserChannelInWaiting(channel2);
        allClientsBase.removeUserChanelFromBase(channel2);
        assertNull(allClientsBase.createNewPairOfUserAndAgent());
        assertEquals(allClientsBase.getClientNameByChanel(channel2),"not authorized");
    }

    @Test
    void removeAgentChanelFromBase() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        allClientsBase.addUserChannelInWaiting(channel2);
        assertNotNull(allClientsBase.createNewPairOfUserAndAgent());
        allClientsBase.removeAgentChanelFromBase(channel1);
        assertNull(allClientsBase.createNewPairOfUserAndAgent());
        assertEquals(allClientsBase.getClientNameByChanel(channel1),"not authorized");
    }

    @Test
    void createNewPairOfUserAndAgent() {
        AllClientsBase allClientsBase=new AllClientsBase();
        allClientsBase.addNewAgent(channel1,"agent");
        allClientsBase.addNewUser(channel2,"user");
        allClientsBase.addUserChannelInWaiting(channel2);
        assertNotNull(allClientsBase.createNewPairOfUserAndAgent());
        allClientsBase.addUserChannelInWaiting(channel2);
        assertNull(allClientsBase.createNewPairOfUserAndAgent());
    }

}