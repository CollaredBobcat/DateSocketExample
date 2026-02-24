import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class DateClient {

    public static void main(String[] args) throws IOException {
        String serverAddress = "localhost";
        int port = 9090;

        try (Socket socket = new Socket(serverAddress, port);
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
             );
             PrintWriter out = new PrintWriter(
                 new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                 true
             );
             BufferedReader console = new BufferedReader(
                 new InputStreamReader(System.in, StandardCharsets.UTF_8)
             )
        ) {
            while (true) {
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Server closed the connection.");
                    return;
                }

                if (response.equals("SUBMIT_NAME")) {
                    System.out.print("Enter your name: ");
                    String name = console.readLine();
                    out.println(name);

                } else if (response.equals("NAME_TAKEN")) {
                    System.out.println("That name is already taken. Try another.");

                } else if (response.equals("NAME_INVALID")) {
                    System.out.println("Name cannot be empty. Try again.");

                } else if (response.startsWith("NAME_ACCEPTED")) {
                    String acceptedName = response.substring("NAME_ACCEPTED".length()).trim();
                    System.out.println("Connected as: " + acceptedName);
                    System.out.println("Type messages. Ctrl+C to quit.");

                    String msg;
                    while ((msg = console.readLine()) != null) {
                        out.println(msg);
                    }
                    return;

                } else {
                    System.out.println(response);
                }
            }
        }
    }
}
