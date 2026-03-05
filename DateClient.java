import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class DateClient {

    public static void main(String[] args) throws IOException {

        String serverAddress = (args.length > 0) ? args[0] : "172.20.10.4";
        int port = 9090;

        Socket socket = new Socket(serverAddress, port);

        try (
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));
        ) {

            while (true) {

                String response = in.readLine();

                if (response == null) {
                    System.out.println("Disconnected.");
                    break;
                }

                if (response.equals("SUBMIT_NAME")) {

                    System.out.print("Enter your name: ");
                    out.println(console.readLine());

                }
                else if (response.equals("NAME_TAKEN")) {

                    System.out.println("Name already taken.");

                }
                else if (response.equals("NAME_INVALID")) {

                    System.out.println("Invalid name.");

                }
                else if (response.startsWith("NAME_ACCEPTED")) {

                    System.out.println("Connected!");
                    System.out.println("Type /list to see connected clients.");

                    String msg;

                    while ((msg = console.readLine()) != null) {

                        out.println(msg);

                        // if requesting list, read until END_LIST
                        if (msg.equalsIgnoreCase("/list")) {

                            String line;

                            while (!(line = in.readLine()).equals("END_LIST")) {
                                System.out.println(line);
                            }
                        }
                    }

                }
                else {
                    System.out.println(response);
                }
            }

        } finally {
            socket.close();
        }
    }
}