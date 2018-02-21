import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.nio.channels.SocketChannel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class UsersSMSCacheTest {

    UsersSMSCache usersSMSCache=new UsersSMSCache();

    @Test
    void addSMSinCache(){
        SocketChannel socketChannel=mock(SocketChannel.class);
        usersSMSCache.addSMSinCache(socketChannel,"1234");
        Assert.assertEquals(usersSMSCache.removeCachedSMS(socketChannel),"1234");
    }

    @Test
    void addSMSinCache2(){
        SocketChannel socketChannel=mock(SocketChannel.class);
        usersSMSCache.addSMSinCache(socketChannel,"1234");
        usersSMSCache.addSMSinCache(socketChannel,"done");
        String sms=usersSMSCache.removeCachedSMS(socketChannel);
        Assert.assertEquals(sms,"1234\ndone");
    }
}