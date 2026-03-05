import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class DateServer {

    // Stores client name -> client info
    private static final ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {

        int port = 9090;

        System.out.println("Host IP: " + InetAddress.getLocalHost().getHostAddress());

        ServerSocket listener = new ServerSocket(port);
        System.out.println("DateServer running on port " + port);

        try {
            while (true) {
                Socket socket = listener.accept();
                new Thread(new ClientHandler(socket)).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            String name = null;

            try (
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            ) {

                // Ask for name
                while (true) {

                    out.println("SUBMIT_NAME");
                    String candidate = in.readLine();

                    if (candidate == null) return;

                    candidate = candidate.trim();

                    if (candidate.isEmpty()) {
                        out.println("NAME_INVALID");
                        continue;
                    }

                    ClientInfo info = new ClientInfo(socket, out);

                    if (clients.putIfAbsent(candidate, info) == null) {
                        name = candidate;
                        out.println("NAME_ACCEPTED " + name);
                        System.out.println(name + " joined.");
                        break;
                    } else {
                        out.println("NAME_TAKEN");
                    }
                }

                // Message loop
                String line;

                while ((line = in.readLine()) != null) {

                    // LIST COMMAND
                    if (line.equalsIgnoreCase("/list")) {

                        out.println("Connected Clients:");

                        for (String clientName : clients.keySet()) {
                            out.println(clientName);
                        }

                        out.println("END_LIST");
                        continue;
                    }

                    // Normal message
                    System.out.println("[" + name + "] " + line);
                }

            } catch (Exception e) {
                System.out.println("Connection error.");
                e.printStackTrace();
            } finally {

                if (name != null) {
                    clients.remove(name);
                    System.out.println(name + " left.");
                }

                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }

    private static class ClientInfo {

        final Socket socket;
        final PrintWriter out;

        ClientInfo(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
        }
    }
}