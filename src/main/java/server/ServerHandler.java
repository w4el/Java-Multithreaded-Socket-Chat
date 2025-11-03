package server;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerHandler extends Thread {
    private Socket socket;
    private final Map<String, ServerHandler> clients;
    static volatile String coordinatorUserID;
    private final AtomicBoolean hasCoordinator;
    private String userID;
    private PrintWriter writer;
    private long lastActiveTime;
    private static final String LOG_FILE_NAME = "server_log.txt";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    public ServerHandler(Socket socket, Map<String, ServerHandler> clients, AtomicBoolean hasCoordinator) {
        this.socket = socket;
        this.clients = clients;
        this.hasCoordinator = hasCoordinator;
        this.lastActiveTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = getWriter(socket.getOutputStream());
    
            boolean uniqueID = false;
            while (!uniqueID) {
                userID = reader.readLine();
                synchronized (clients) {
                    if (clients.containsKey(userID)) {
                        writer.println("ID_TAKEN");
                    } else {
                        clients.put(userID, this);
                        writer.println("ID_ACCEPTED");
                        uniqueID = true;
    
                        
                        uniqueID = true; // Break the loop as the ID is now verified
                        if (hasCoordinator.compareAndSet(false, true)) {
                            coordinatorUserID = userID;
                            writer.println("you are the coordinator.");
                            writer.println("You are the coordinator.");
                        } else {
                            writer.println("welcome " + userID + " the current coordiantor is "+ coordinatorUserID); 
                            writer.println("Welcome " + userID + " the current coordiantor is "+ coordinatorUserID);
                        }
                        broadcastSystemMessage(userID + " has joined the chat.", null);
                    }
                }
            }
    
            // Notify the client about their role (coordinator or regular user)
            
    
            String message;
        while ((message = reader.readLine()) != null) {
            logMessage("Received from " + userID + ": " + message, true); // Log incoming message
            if ("QUIT".equals(message)) {
                break;


            } else if ("REQUEST_DETAILS".equals(message)) {
                handleRequestDetails(userID);


            } else if (message.startsWith("APPROVE_DETAILS ")) {
                handleApproveDetails(message);


            } else if (message.startsWith("DENY_DETAILS ")) {
                handleDenyDetails(message);
                
            } else if ("PING_MEMBERS".equals(message) && userID.equals(coordinatorUserID)) {
                // The coordinator sends a ping to all members
                for (ServerHandler handler : clients.values()) {
                    if (!handler.userID.equals(coordinatorUserID)) {
                        handler.sendMessage("PING_REQUEST");
                    }
                }
            } else if (message.startsWith("@")) {
                handlePrivateMessage(message);
            } else {
                broadcastMessage(userID + ": " + message, userID);
            }
        }
    } catch (IOException e) {
        System.err.println("Error handling client " + userID + ": " + e.getMessage());
    } finally {
        if (!socket.isClosed()) {
            cleanUp();
            }
        }
    }
     protected PrintWriter getWriter(OutputStream outputStream) {
        return new PrintWriter(outputStream, true);
    }
    private void logMessage(String message, boolean isIncoming) {
        String timestamp = LocalDateTime.now().format(dtf);
        String logEntry = String.format("%s [%s] %s", timestamp, isIncoming ? "INCOMING" : "OUTGOING", message);

        synchronized (ServerHandler.class) {
            try (FileWriter fw = new FileWriter(LOG_FILE_NAME, true);
                 PrintWriter logWriter = new PrintWriter(fw, true)) {
                logWriter.println(logEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    // Add a setter method for userID
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public String getUserID() {
        return this.userID;
    }
    

    private void handleRequestDetails(String senderID) {
        if (!senderID.equals(coordinatorUserID)) {
            // Send request directly to the coordinator
            sendMessageToUser("DETAILS_REQUEST_FROM " + senderID, coordinatorUserID);
        }
        
    }
    
    // Method to handle "APPROVE_DETAILS" response from the coordinator
    private void handleApproveDetails(String message) {
        String[] parts = message.split(" ");
        if (parts.length > 1) {
            String approvedUserID = parts[1];
            // Directly send the member details to the requester
            sendMemberDetails(approvedUserID);
        }
    }
    
    // Method to handle "DENY_DETAILS" response from the coordinator
    private void handleDenyDetails(String message) {
        String[] parts = message.split(" ");
        if (parts.length > 1) {
            String deniedUserID = parts[1];
            // Directly notify the requester about the denial
            sendMessageToUser("DETAILS_DENIED", deniedUserID);
        }
    }
    
    // Utility method to send a message to a specific user
    void sendMessageToUser(String message, String targetUserID) {
        ServerHandler targetUser = clients.get(targetUserID);
        if (targetUser != null) {
            targetUser.sendMessage(message);
        }
    }


    public void sendPingRequest() {
        sendMessage("PING_REQUEST");
    }


    public void handlePongResponse() {
        updateLastActiveTime();
    }


    public void updateLastActiveTime() {
        lastActiveTime = System.currentTimeMillis();
    }


    public void cleanUpInactiveClients() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActiveTime > 30000) { // 30 seconds
            cleanUp();
        }
    }


    private void sendMemberDetails(String targetUserID) {
        StringBuilder details = new StringBuilder("MEMBER DETAILS:\n");
        synchronized (clients) {
            clients.forEach((id, handler) -> {
                Socket clientSocket = handler.socket;
                details.append(String.format("ID: %s, IP: %s, Port: %d\n",
                        id, clientSocket.getInetAddress().getHostAddress(), clientSocket.getPort()));
            });
            details.append("COORDINATOR: ").append(coordinatorUserID);
            // Use the sendMessageToUser method to send details to the specific user
            sendMessageToUser(details.toString(), targetUserID);
        }
    }
    
    
    void cleanUp() {
        synchronized (clients) {
            if (clients.remove(userID) != null) {
                logMessage("Session ended for user " + userID, false); // Log session end
                broadcastSystemMessage(userID + " has left the chat.", null);
            }


            
            if (userID.equals(coordinatorUserID)) {
                if (!clients.isEmpty()) {
                    assignNewCoordinator();
                } else {
                    coordinatorUserID = null; // Reset coordinator if no clients are left
                    hasCoordinator.set(false);
                }
            }
        }
        closeResources();
    }


    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket for userID " + userID + ": " + e.getMessage());
        }
    }


    private void assignNewCoordinator() {
        String newCoordinatorID = clients.keySet().iterator().next();
        coordinatorUserID = newCoordinatorID;
        ServerHandler newCoordinator = clients.get(newCoordinatorID);
        newCoordinator.sendMessage("You are now the coordinator.");
        broadcastSystemMessage(newCoordinator.userID + " is now the coordinator.", null);
    }


    void broadcastMessage(String message, String senderID) {
        clients.values().forEach(client -> {
            if (!client.userID.equals(senderID)) { // Check if the client is not the sender
                client.sendMessage(message);
            }
        });
        logMessage("Broadcasted: " + message, false); // Log broadcasted message
    }
    
    


    private void broadcastSystemMessage(String message, String excludeUserID) {
        clients.forEach((id, clientHandler) -> {
            if (!id.equals(excludeUserID)) {
                clientHandler.sendMessage("SYSTEM: " + message);
            }
        });
        System.out.println("SYSTEM: " + message);
        logMessage("System broadcast: " + message, false);
    }


    void handlePrivateMessage(String message) {
        // Find the end of the username part (first non-alphanumeric character after @)
        int endOfUsernameIndex = 1; // Start from 1 because 0 is '@'
        while (endOfUsernameIndex < message.length() && Character.isLetterOrDigit(message.charAt(endOfUsernameIndex))) {
            endOfUsernameIndex++;
        }
    
        // Extract the username and the actual message
        String targetUsername = message.substring(1, endOfUsernameIndex); // Extract username directly after @
        String privateMessage = message.substring(endOfUsernameIndex).trim(); // The rest is the message
    
        // Find the target user and send the private message
        clients.values().stream()
            .filter(clientHandler -> clientHandler.userID.equals(targetUsername))
            .findFirst()
            .ifPresent(clientHandler -> {
                
                String formattedMessage = String.format("%s(private): %s", this.userID, privateMessage);
                clientHandler.sendMessage(formattedMessage);
            });
        logMessage("Private message handled for " + userID + ": " + message, false);
          
    }


    void sendMessage(String message) {
        try {
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(message);
            logMessage("Sent to " + userID + ": " + message, false); // Log outgoing message
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void getBufferedReader(Object any) {
        throw new UnsupportedOperationException("Unimplemented method 'getBufferedReader'");
    }

}