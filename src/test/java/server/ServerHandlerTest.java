package server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ServerHandlerTest {

    @Mock
    private Socket socket;
    private Map<String, ServerHandler> clients;
    private AtomicBoolean hasCoordinator;
    private ServerHandler serverHandler;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        clients = new HashMap<>();
        hasCoordinator = new AtomicBoolean(false);
        when(socket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(socket.getInputStream()).thenReturn(new ByteArrayInputStream("".getBytes()));
        serverHandler = new ServerHandler(socket, clients, hasCoordinator);
    }
    @Test
void testSendMessage() throws IOException {
    String testMessage = "Test Message";
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    when(socket.getOutputStream()).thenReturn(baos);
    serverHandler.sendMessage(testMessage);

    // Assert that the message was written to the OutputStream
    String output = baos.toString();
    assertTrue(output.contains(testMessage));
}
@Test
void testRunForUniqueIDAndQuit() throws Exception {
    String userID = "TestUser\nQUIT\n";
    when(socket.getInputStream()).thenReturn(new ByteArrayInputStream(userID.getBytes()));
    // Start the server handler in a separate thread to mimic real-life usage
    Thread thread = new Thread(serverHandler);
    thread.start();
    thread.join(1000); // Wait for the thread to process

    
}
@Test
void testSendMessageToUser() {
    ServerHandler mockServerHandler = mock(ServerHandler.class);
    clients.put("recipient", mockServerHandler);
    serverHandler.sendMessageToUser("Hello, World!", "recipient");

    // Verify sendMessage was called on the mockServerHandler with the correct message
    verify(mockServerHandler, times(1)).sendMessage("Hello, World!");
}
@Test
void testCleanUp() {
    // Prepopulate the clients map and simulate disconnection
    String testUserID = "TestUser";
    serverHandler.setUserID(testUserID); 
    clients.put(testUserID, serverHandler);

    serverHandler.cleanUp();
    assertFalse(clients.containsKey(testUserID));
   
}


}
