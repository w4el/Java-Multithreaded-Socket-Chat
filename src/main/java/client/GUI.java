package client;

import javax.swing.*;
import java.awt.*;

public class GUI {
    JFrame frame;
    JTextArea messageArea;
    JTextField inputField;
    private JButton sendButton, quitButton, requestDetailsButton, pingMembersButton;
    private Client client;
    public boolean isCoordinator = false;
    public boolean isMember = false;
    private boolean isTestingMode;
    public GUI(Client client) {
        this.client = client;
        createAndShowGUI();
    }
    public GUI(Client client, boolean isTestingMode) {
        this.client = client;
        this.isTestingMode = isTestingMode; // Initialize the variable with the value passed to the constructor
        if (!this.isTestingMode) {
            createAndShowGUI();
        }
    }
    private void createAndShowGUI() {
        frame = new JFrame("Chat Client");
        messageArea = new JTextArea(20, 40);
        inputField = new JTextField(40);
        sendButton = new JButton("Send");
        quitButton = new JButton("Quit");
        requestDetailsButton = new JButton("Request Details");
        pingMembersButton = new JButton("Ping Members");

        messageArea.setEditable(false);

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(inputField, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(sendButton);
        buttonPanel.add(quitButton);
        buttonPanel.add(requestDetailsButton);
        buttonPanel.add(pingMembersButton);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        frame.add(southPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> client.sendMessage(inputField.getText().trim()));
        inputField.addActionListener(e -> client.sendMessage(inputField.getText().trim()));
        quitButton.addActionListener(e -> client.quitApplication());
        requestDetailsButton.addActionListener(e -> client.requestDetails());
        pingMembersButton.addActionListener(e -> client.sendPingToMembers());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        requestDetailsButton.setVisible(false);
        pingMembersButton.setVisible(false);
    }

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message));
    }

    public void processServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.equals("you are the coordinator.") || message.equals("You are now the coordinator.")) {
                isCoordinator = true;
                isMember = false;
                pingMembersButton.setVisible(true);
                requestDetailsButton.setVisible(false);
            } else if (message.startsWith("welcome ")) {
                isMember = true;
                isCoordinator = false;
                requestDetailsButton.setVisible(true);
                pingMembersButton.setVisible(false); // Hide as this is for coordinators only
            } else if ("PING_REQUEST".equals(message)) {
                Timer timer = new Timer(20000, (e) -> {
                    client.quitApplication();
                });
                timer.setRepeats(false); // Ensure it only triggers once
                timer.start();

                int response = JOptionPane.showConfirmDialog(frame, "Are you still active?", "Ping Request", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    // If the user responds within 20 seconds, stop the timer
                    timer.stop();
                    client.writer.println("PONG");
                } else {
                    client.quitApplication(); // If the user clicks no, quit immediately
                }
            } else if (message.startsWith("DETAILS_REQUEST_FROM ")) {
                String requestingUser = message.substring("DETAILS_REQUEST_FROM ".length());
                int approve = JOptionPane.showConfirmDialog(frame, "Approve details request from " + requestingUser + "?", "Approve Request", JOptionPane.YES_NO_OPTION);
                if (approve == JOptionPane.YES_OPTION) {
                    client.writer.println("APPROVE_DETAILS " + requestingUser);
                } else {
                    client.writer.println("DENY_DETAILS " + requestingUser);
                }
            } else if (message.startsWith("APPROVE_DETAILS") || message.startsWith("DETAILS_DENIED")) {
                JOptionPane.showMessageDialog(frame, message, "Response", JOptionPane.INFORMATION_MESSAGE);
            
            }else if (isTestingMode) {
                // Handle message without GUI interaction, or simply print to console for testing
                System.out.println("Processing message in testing mode: " + message);
            } else {
                appendMessage(message + "\n");
            }
        });
    }

}

