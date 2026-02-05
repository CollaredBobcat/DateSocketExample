import java.net.*;
import java.io.*;

public class DateServer {
    public static void main(String[] args) {
        int port = 6013;

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("DateServer listening on port " + port);

            while (true) {
                try (
		Socket client = server.accept();
		     BufferedReader in = new BufferedReader(
			  new InputStreamReader(client.getInputStream()));
                     PrintWriter out = new PrintWriter(
			client.getOutputStream(), true)
			) {

                    System.out.println("Connected: " + client.getInetAddress());
		    String message = in.readLine();
		    System.out.println("Client says: " + message);
                    out.println(new java.util.Date().toString());
                }
            }
        } catch (IOException ioe) {
            System.err.println("Server error: " + ioe);
        }
    }
}
