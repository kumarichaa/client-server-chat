import java.net.*;
import java.util.*;

public class UDPServer {
    // Store known client addresses and ports
    private static Set<ClientInfo> clients = new HashSet<>();

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(1234)) {
            byte[] buffer = new byte[1024];
            System.out.println("\u001B[34m[UDP Server started on port 1234...]\u001B[0m");

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String receivedMsg = new String(packet.getData(), 0, packet.getLength());
                InetAddress senderAddress = packet.getAddress();
                int senderPort = packet.getPort();

                // Add new client if not already tracked
                clients.add(new ClientInfo(senderAddress, senderPort));

                String broadcastMsg = "Client[" + senderPort + "]: " + receivedMsg;

                if (clients.size() == 1) {
                    // Only one client connected: reply directly
                    String reply = "Server: You are the only client online. Message received: " + receivedMsg;
                    byte[] replyBuffer = reply.getBytes();
                    DatagramPacket replyPacket = new DatagramPacket(
                        replyBuffer, replyBuffer.length,
                        senderAddress, senderPort
                    );
                    socket.send(replyPacket);
                } else {
                    // Multiple clients: broadcast to others
                    for (ClientInfo client : clients) {
                        if (!(client.address.equals(senderAddress) && client.port == senderPort)) {
                            byte[] sendBuffer = broadcastMsg.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(
                                sendBuffer, sendBuffer.length,
                                client.address, client.port
                            );
                            socket.send(sendPacket);
                        }
                    }
                }

                System.out.println("Message from [" + senderPort + "]: " + receivedMsg);
            }

        } catch (Exception e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    // Helper class to track client information
    static class ClientInfo {
        InetAddress address;
        int port;

        ClientInfo(InetAddress address, int port) {
            this.address = address;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ClientInfo)) return false;
            ClientInfo c = (ClientInfo) o;
            return port == c.port && address.equals(c.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(address, port);
        }
    }
}
