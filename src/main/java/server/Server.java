package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.UnknownHostException;


public class Server {
    private final int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private ServerSocket serverSocket;
    private static final Map<String, ServerHandler> clients = new ConcurrentHashMap<>();
    private static final AtomicBoolean hasCoordinator = new AtomicBoolean(false);

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        running.set(true);
    try {
        serverSocket = new ServerSocket(port);
        try {
            // Attempt to retrieve and print the local IP address
            String localIP = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server is running on IP: " + localIP + ", Port: " + port);
        } catch (UnknownHostException e) {
            // Fallback if the local host name could not be resolved into an address
            System.out.println("Server is running on Port: " + port + " (local IP address could not be determined)");
        }
            startPingRequests();

            while (running.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    ServerHandler handler = new ServerHandler(socket, clients, hasCoordinator);
                    handler.start();
                } catch (IOException e) {
                    if (!running.get()) {
                        // Server was asked to stop
                        System.out.println("Server stopping.");
                        break;
                    }
                    System.err.println("Exception accepting client connection: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            stop();
        }
    }

    public void stop() {
        running.set(false);
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void startPingRequests() {
    }
        
    public boolean isRunning() {
        return running.get();
    }

    public static void main(String[] args) {
        int port = 1234;
        Server server = new Server(port);
        server.start();
    }
}
