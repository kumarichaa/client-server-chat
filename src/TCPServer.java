
import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class TCPServer {

    private static final Logger logger = Logger.getLogger(TCPServer.class.getName());
    private static final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private static int clientCounter = 1;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown detected. Cleaning up database...");
            deleteAllClientsFromDatabase();
        }));
    }

    public static void main(String[] args) {
        setupLogger();

        try (ServerSocket serverSocket = new ServerSocket(1234)) {
            logger.info("\u001B[34m[TCP Server started on port 1234...]\u001B[0m");

            while (true) {
                Socket socket = serverSocket.accept();
                String clientName = "Client-" + clientCounter++;
                ClientHandler handler = new ClientHandler(socket, clientName);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server Error: ", e);
        } finally {
            flushAllClients();
            deleteAllClientsFromDatabase();
        }
    }

    private static void flushAllClients() {
        logger.info("Flushing all connected clients...");
        for (ClientHandler client : clients) {
            try {
                client.socket.close();
                logger.info("Closed socket for: " + client.clientName);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing client socket: " + client.clientName, e);
            }
        }
        clients.clear();
    }

    private static void deleteAllClientsFromDatabase() {
        logger.info("Deleting all clients from database...");

        String url = "jdbc:mysql://localhost:3306/chatdb";
        String user = "root";
        String password = "1234";

        try (Connection conn = DriverManager.getConnection(url, user, password);
                Statement stmt = conn.createStatement()) {

            int deleted = stmt.executeUpdate("DELETE FROM clients");
            logger.info("Deleted " + deleted + " client records from database.");

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting clients from database", e);
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String clientName;

        public ClientHandler(Socket socket, String name) {
            this.socket = socket;
            this.clientName = name;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Read actual client name sent by client
                String receivedName = in.readLine();
                this.clientName = receivedName != null && !receivedName.isEmpty() ? receivedName : clientName;

                out.println("Welcome " + clientName + "!");
                UUID clientId = UUID.randomUUID();

                // Store client in database
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatdb", "root",
                        "1234");
                        PreparedStatement stmt = conn.prepareStatement("""
                                    INSERT INTO clients (id, name, status,protocol)
                                    VALUES (?, ?, ?,?)
                                    ON DUPLICATE KEY UPDATE status=VALUES(status)
                                """)) {
                    stmt.setString(1, clientId.toString());
                    stmt.setString(2, clientName);
                    stmt.setString(3, "online");
                    stmt.setString(4, "TCP");
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    logger.log(Level.WARNING, "Database error for " + clientName, e);
                }

                logger.info(clientName + " connected.");

                // Start receiving messages...
                String message;
                while ((message = in.readLine()) != null) {
                    String fullMessage = clientName + ": " + message;
                    logger.info(fullMessage);

                    if (clients.size() == 1) {
                        out.println(
                                "You are the only client available, so talk to me or wait for joining other clients");
                    } else {
                        for (ClientHandler client : clients) {
                            if (client != this) {
                                client.out.println(fullMessage);
                            }
                        }
                    }
                }

            } catch (IOException e) {
                logger.log(Level.WARNING, clientName + " error: ", e);
            } finally {
                try {
                    clients.remove(this);
                    socket.close();
                    logger.info(clientName + " disconnected.");

                    // 🗑️ Delete client from database on disconnect
                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatdb", "root",
                            "1234");
                            PreparedStatement stmt = conn.prepareStatement("""
                                        DELETE FROM clients WHERE name = ?
                                    """)) {
                        stmt.setString(1, clientName);
                        int rows = stmt.executeUpdate();
                        if (rows > 0) {
                            logger.info("Deleted client record for " + clientName);
                        } else {
                            logger.warning("Client not found in database: " + clientName);
                        }
                    } catch (SQLException e) {
                        logger.log(Level.WARNING, "Error deleting client from database: " + clientName, e);
                    }

                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error closing socket for " + clientName, e);
                }
            }

        }
    }

    private static void setupLogger() {
        Logger rootLogger = Logger.getLogger("");
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new SimpleFormatter());
        rootLogger.setLevel(Level.INFO);
        rootLogger.addHandler(consoleHandler);
    }
}
