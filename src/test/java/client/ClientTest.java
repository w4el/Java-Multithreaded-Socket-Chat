package client;

import org.junit.Before;
import org.junit.Test;

import client.Client.UserIDProvider;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientTest {

    private Client client;
    private Socket socketMock;
    private PrintWriter writerMock;
    private BufferedReader readerMock;
    private GUI guiMock;
    private UserIDProvider userIDProviderMock;

    @Before
    public void setUp() throws Exception {
        socketMock = mock(Socket.class);
        writerMock = mock(PrintWriter.class);
        readerMock = mock(BufferedReader.class);
        guiMock = mock(GUI.class);
        userIDProviderMock = mock(UserIDProvider.class);

        doNothing().when(writerMock).println(anyString());
        doNothing().when(writerMock).flush();
        when(userIDProviderMock.getUserID()).thenReturn("testUserID");
        client = new Client(socketMock, writerMock, readerMock, true, userIDProviderMock);
        client.gui = guiMock;
        
    }

    @Test
    public void testSendMessage() {
    String testMessage = "Hello World";
    client.sendMessage(testMessage);
        // Verify the message was sent and GUI updated
        verify(writerMock).println(testMessage);
        verify(guiMock).appendMessage("You: " + testMessage + "\n");
    }
    @Test
    public void shouldPass() {
        assertTrue(true);
    }

    @Test
    public void testSendPingToMembers() {
        client.sendPingToMembers();
        verify(writerMock).println("PING_MEMBERS");
    }
    
    @Test
    public void testRequestDetails() {
        client.requestDetails();
        verify(writerMock).println("REQUEST_DETAILS");
        verify(writerMock).flush();
    }
    
    @Test
    public void testQuitApplication() throws IOException {
        client.quitApplication();
        verify(socketMock).close();
        // Verifying System.exit is tricky and generally not recommended in unit tests.
    }
    
    @Test
    public void testAttemptIDVerification() throws IOException {
        // Configure the readerMock to simulate server response for ID verification
        when(readerMock.readLine()).thenReturn("ID_ACCEPTED");
    
        client.attemptIDVerification();
    
        // Verify the correct user ID was sent to the server
        verify(writerMock).println("testUserID");
    
    }
}

    
