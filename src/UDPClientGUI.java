
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.net.*;

public class UDPClientGUI extends JFrame {
    private JTextPane chatArea;
    private StyledDocument doc;
    private JTextField inputField;
    private JButton sendButton;
    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort = 1234; 
    private String clientName;

    public UDPClientGUI() throws Exception {

        // Prompt for client name
        clientName = JOptionPane.showInputDialog(this, "Enter your name:", "Client Login", JOptionPane.PLAIN_MESSAGE);
        if (clientName == null || clientName.trim().isEmpty()) {
            clientName = "Anonymous";
        }

        setTitle("UDP Client - " + clientName);
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Set background color for the main window
        getContentPane().setBackground(new Color(128, 0, 128)); // Purple

        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(245, 245, 245));
        chatArea.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        doc = chatArea.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(chatArea);

        inputField = new JTextField();
        inputField.setToolTipText("Type your message here...");
        inputField.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        inputField.setBackground(new Color(255, 255, 240));

        sendButton = new JButton("Send 🚀");
        sendButton.setBackground(new Color(0, 153, 255));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);

        // Emoji picker panel
        JPanel emojiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 2));
        emojiPanel.setBackground(new Color(230, 240, 255));
        String[] emojis = { "😀", "😂", "😍", "😎", "👍", "🙏", "🎉", "😢", "😡", "❤️", "🔥", "🥳" };
        for (String emoji : emojis) {
            JButton emojiBtn = new JButton(emoji);
            emojiBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
            emojiBtn.setMargin(new Insets(2, 2, 2, 2));
            emojiBtn.setFocusPainted(false);
            emojiBtn.setBorderPainted(false);
            emojiBtn.setContentAreaFilled(false);
            emojiBtn.addActionListener(e -> inputField.setText(inputField.getText() + emoji));
            emojiPanel.add(emojiBtn);
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(230, 240, 255));
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        // Add emoji panel below input field
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBackground(new Color(230, 240, 255));
        southPanel.add(panel, BorderLayout.NORTH);
        southPanel.add(emojiPanel, BorderLayout.SOUTH);

        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        socket = new DatagramSocket();
        serverAddress = InetAddress.getByName("localhost");

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        // Receiver thread to handle incoming messages
        new Thread(() -> {
            try {
                while (true) {
                    byte[] recvBuffer = new byte[1024];
                    DatagramPacket reply = new DatagramPacket(recvBuffer, recvBuffer.length);
                    socket.receive(reply);
                    String replyMsg = new String(reply.getData(), 0, reply.getLength());
                    appendMessage(replyMsg, new Color(0, 102, 51), false);
                }
            } catch (Exception ex) {
                appendMessage("Error receiving message: " + ex.getMessage(), Color.RED, false);
            }
        }).start();
    }

    // Send message to server
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            try {
                String fullMsg = clientName + ": " + msg;
                byte[] buffer = fullMsg.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);
                socket.send(packet);
                appendMessage("You: " + msg, Color.BLUE, true);
                inputField.setText("");
            } catch (Exception ex) {
                appendMessage("Error sending message: " + ex.getMessage(), Color.RED, false);
            }
        }
    }

    private void appendMessage(String message, Color color, boolean bold) {
        Style style = chatArea.addStyle("Style", null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
        try {
            doc.insertString(doc.getLength(), message + "\n", style);
            chatArea.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new UDPClientGUI().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
