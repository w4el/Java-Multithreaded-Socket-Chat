package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Client {
    private Socket socket;
    PrintWriter writer;
    private BufferedReader reader;
    public GUI gui;
    public String userID;
    private static boolean isTestingMode;
    private static UserIDProvider userIDProvider;

    public interface UserIDProvider {
        String getUserID();
    }

    // This class is used to get the userID in a GUI environment
    public static class GUIUserIDProvider implements UserIDProvider {
        @Override
        public String getUserID() {
            return JOptionPane.showInputDialog("Enter your ID:");
        }
    }

    // Constructor modified to accept isTestingMode and UserIDProvider as parameters
    public Client(Socket socket, PrintWriter writer, BufferedReader reader, boolean isTestingMode, UserIDProvider userIDProvider) {
        this.socket = socket;
        this.writer = writer;
        this.reader = reader;
        Client.isTestingMode = isTestingMode;
        Client.userIDProvider = userIDProvider; // Use the provided UserIDProvider

        // GUI initialization is now conditional based on isTestingMode
        if (!isTestingMode) {
            this.gui = new GUI(this, isTestingMode);
        }
        connectToServer();
    }

    private void connectToServer() {
        if (!isTestingMode) {
            attemptIDVerification();
        }
        readMessages();
    }

    public void sendMessage(String message) {
        writer.println(message);
        if(gui != null) {
            gui.appendMessage("You: " + message + "\n");
        }
    }

    public void sendPingToMembers() {
        writer.println("PING_MEMBERS");
    }

    public void requestDetails() {
        writer.println("REQUEST_DETAILS");
        writer.flush();
    }

    public void quitApplication() {
        try {
            if (writer != null) {
                writer.println("QUIT");
                writer.flush();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!isTestingMode) {
                System.exit(0);
            }
        }
    }
    
    void attemptIDVerification() {
        while (true) {
            userID = userIDProvider.getUserID();
            if (userID == null || userID.isEmpty()) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "You must enter a valid ID. Please try again.", "Invalid ID", JOptionPane.ERROR_MESSAGE));
                continue;
            }
            writer.println(userID);
            writer.flush();

            try {
                String serverResponse = reader.readLine();
                if ("ID_TAKEN".equals(serverResponse)) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "ID is already taken. Please choose another ID.", "ID Taken", JOptionPane.WARNING_MESSAGE));
                } else {
                    break;
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Error reading from server. Please try reconnecting.", "Connection Error", JOptionPane.ERROR_MESSAGE));
                System.exit(1);
            }
        }
    }

    void readMessages() {
        new Thread(() -> {
            try {
                String fromServer;
                while ((fromServer = reader.readLine()) != null) {
                    if(gui != null) {
                        gui.processServerMessage(fromServer);
                    }
                }
            } catch (IOException e) {
                if (!isTestingMode) {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Lost connection to server.", "Connection Error", JOptionPane.ERROR_MESSAGE));
                    System.exit(1);
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            userIDProvider = new GUIUserIDProvider(); // Use GUI to get the user ID
            Client client = createWithRealConnection();
            if (client == null) {
                System.exit(1);
            }
        });
    }

    public static Client createWithRealConnection() {
        try {
            String hostname = JOptionPane.showInputDialog(null, "Enter server IP address:", "localhost");
            int port = Integer.parseInt(JOptionPane.showInputDialog(null, "Enter server port:", "1234"));
            Socket socket = new Socket(hostname, port);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return new Client(socket, writer, reader, false, userIDProvider);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to server. Please try again later.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return null;
        }
    }
}