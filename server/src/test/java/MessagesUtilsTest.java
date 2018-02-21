import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * Created by Alex on 16.02.2018.
 */
class MessagesUtilsTest {
    MessagesUtils messagesUtils=new MessagesUtils();

    @Test
    void isSignInUserMessage() {
        Assert.assertFalse(messagesUtils.isSignInUserMessage("sdaffsad"));
        Assert.assertTrue(messagesUtils.isSignInUserMessage("/register user alex"));
        Assert.assertFalse(messagesUtils.isSignInUserMessage("register user alex"));
        Assert.assertFalse(messagesUtils.isSignInUserMessage("register ussder alex"));
        Assert.assertFalse(messagesUtils.isSignInUserMessage("/register agent alex"));
        Assert.assertTrue(messagesUtils.isSignInUserMessage("/register user alex4312"));
        Assert.assertFalse(messagesUtils.isSignInUserMessage("/register user alex4312 sd"));
    }

    @Test
    void isSignInAgentMessage() {
        Assert.assertFalse(messagesUtils.isSignInAgentMessage("sad"));
        Assert.assertTrue(messagesUtils.isSignInAgentMessage("/register agent alex"));
        Assert.assertFalse(messagesUtils.isSignInAgentMessage("register agent alex"));
        Assert.assertFalse(messagesUtils.isSignInAgentMessage("register saagent alex"));
        Assert.assertFalse(messagesUtils.isSignInAgentMessage("/register user alex"));
        Assert.assertTrue(messagesUtils.isSignInAgentMessage("/register agent alex4312"));
        Assert.assertFalse(messagesUtils.isSignInAgentMessage("/register agent alex4312 sd"));
    }

    @Test
    void isSignInMessage() {
        Assert.assertFalse(messagesUtils.isSignInMessage("sad"));
        Assert.assertTrue(messagesUtils.isSignInMessage("/register agent alex"));
        Assert.assertTrue(messagesUtils.isSignInMessage("/register user alex"));

        Assert.assertFalse(messagesUtils.isSignInMessage("register user alex"));
        Assert.assertFalse(messagesUtils.isSignInMessage("register saagent alex"));

        Assert.assertTrue(messagesUtils.isSignInMessage("/register agent alex4312"));
        Assert.assertFalse(messagesUtils.isSignInMessage("/register agent alex4312 sd"));
    }

    @Test
    void isExitMessage() {
        Assert.assertTrue(messagesUtils.isExitMessage("/exit"));
        Assert.assertFalse(messagesUtils.isExitMessage("exit"));
        Assert.assertFalse(messagesUtils.isExitMessage("/exitsa"));
        Assert.assertFalse(messagesUtils.isExitMessage("/leave"));
    }

    @Test
    void isLeaveMessage() {
        Assert.assertTrue(messagesUtils.isLeaveMessage("/leave"));
        Assert.assertFalse(messagesUtils.isLeaveMessage("leave"));
        Assert.assertFalse(messagesUtils.isLeaveMessage("/leavesd"));
        Assert.assertFalse(messagesUtils.isLeaveMessage("/exit"));
    }

    @Test
    void getNameFromMessage() {
        Assert.assertEquals(messagesUtils.getNameFromMessage("/register user alex"),"alex");
        Assert.assertEquals(messagesUtils.getNameFromMessage("/register agent afdasdfasd"),"afdasdfasd");
    }

    @Test
    void getMessageType() {
        Assert.assertEquals(messagesUtils.getMessageType("/register user alex"),Constants.MESSAGE_TYPE_REGISTER);
        Assert.assertEquals(messagesUtils.getMessageType("user alex"),Constants.MESSAGE_TYPE_SMS);
        Assert.assertEquals(messagesUtils.getMessageType("/leave"),Constants.MESSAGE_TYPE_LEAVE);
        Assert.assertEquals(messagesUtils.getMessageType("/exit"),Constants.MESSAGE_TYPE_EXIT);
    }

}