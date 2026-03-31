import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class DateServer {

    private static final ConcurrentHashMap<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        int port = 9090;

        ServerSocket listener = new ServerSocket(port);
        System.out.println("DateServer running on port " + port);

        try {
            while (true) {
                Socket socket = listener.accept();
                System.out.println("[SERVER] Accepted connection from " + socket.getRemoteSocketAddress());
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

        @Override
        public void run() {
            String name = null;

            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
                );
                PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                    true // autoFlush on println
                )
            ) {
                // 1) Ask for name, enforce unique
                while (true) {
                    out.println("SUBMIT_NAME");

                    String candidate = in.readLine();
                    if (candidate == null) {
                        // client disconnected before providing name
                        System.out.println("[SERVER] Client disconnected during name submission: "
                                + socket.getRemoteSocketAddress());
                        return;
                    }

                    candidate = candidate.trim();
                    if (candidate.isEmpty()) {
                        out.println("NAME_INVALID");
                        continue;
                    }

                    ClientInfo info = new ClientInfo(socket, out);
                    ClientInfo existing = clients.putIfAbsent(candidate, info);

                    if (existing == null) {
                        name = candidate;
                        out.println("NAME_ACCEPTED " + name);
                        System.out.println("[SERVER] " + name + " connected from " + socket.getRemoteSocketAddress());
                        break;
                    } else {
                        out.println("NAME_TAKEN");
                    }
                }

                // Read and display all messages from this client
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("[" + name + "] " + line);
                }

                System.out.println("[SERVER] " + name + " closed the connection normally.");

            } catch (Exception e) {
                // If something goes wrong, we WANT to see it, because it explains the client's "reset"
                System.out.println("[SERVER] Handler crashed for " + socket.getRemoteSocketAddress()
                        + (name != null ? " (" + name + ")" : ""));
                e.printStackTrace(System.out);
            } finally {
                if (name != null) {
                    clients.remove(name);
                    System.out.println("[SERVER] Removed client " + name);
                }
                try { socket.close(); } catch (IOException ignored) {}
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
