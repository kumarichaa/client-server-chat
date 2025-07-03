
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;

public class TCPClientGUI {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private String clientName;

    public TCPClientGUI(String server, int port) throws IOException {
        // Ask for client name
        clientName = JOptionPane.showInputDialog(null, "Enter your name:", "Client Name", JOptionPane.PLAIN_MESSAGE);
        if (clientName == null || clientName.trim().isEmpty()) {
            clientName = "Anonymous";
        }

        socket = new Socket(server, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        out.println(clientName);

        frame = new JFrame("TCP Client - " + clientName);
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        inputField = new JTextField(30);
        sendButton = new JButton("Send");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Event Listeners
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // Receiving messages from server
        new Thread(() -> {
            try {
                String serverReply;
                while ((serverReply = in.readLine()) != null) {
                    chatArea.append(serverReply + "\n");
                }
            } catch (IOException ex) {
                chatArea.append("Connection closed.\n");
            }
        }).start();
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            String formatted = clientName + ": " + message;
            out.println(formatted);
            chatArea.append("You: " + message + "\n");
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new TCPClientGUI("localhost", 1234);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Unable to connect to server.");
            }
        });
    }
}
