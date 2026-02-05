import java.net.*;
import java.io.*;

public class DateClient {
    public static void main(String[] args) {
        String host = "172.20.10.9"; // localhost
        int port = 6013;

        try (Socket sock = new Socket(host, port);
        PrintWriter out = new PrintWriter(
            sock.getOutputStream(), true);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(sock.getInputStream()))) {

            out.println("hello!");
            String response = in.readLine();
            System.out.println("Server date: " + response);
        } catch (IOException ioe) {
            System.err.println("Client error: " + ioe);
        }
    }
}
