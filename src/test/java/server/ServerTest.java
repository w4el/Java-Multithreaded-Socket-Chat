package server;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

    private Server server;
    private final int testPort = 12345; // Use a different port to avoid conflicts

    @BeforeEach
    public void setUp() {
        server = new Server(testPort);
    }

    @Test
    public void testServerLifecycle() throws InterruptedException {
        // Start server in a separate thread to prevent blocking the test
        Thread serverThread = new Thread(server::start);
        serverThread.start();

        // Allow some time for the server to start
        Thread.sleep(1000); // Adjust this based on your system's speed

        assertTrue(server.isRunning(), "Server should be running after start");

        // Stop the server
        server.stop();

        // Allow some time for the server to stop
        Thread.sleep(1000); // Adjust this based on your system's speed

        assertFalse(server.isRunning(), "Server should not be running after stop");

        // Clean up
        serverThread.join();
    }

    @AfterEach
    public void tearDown() {
        // Ensure the server is stopped after each test
        if (server.isRunning()) {
            server.stop();
        }
    }
}
