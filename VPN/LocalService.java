import java.io.*;
import java.net.*;

public class LocalService {
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("Local service listening on port 8888");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + clientSocket.getInetAddress());

            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

            try {
                while (true) {
                    int length = in.readInt();
                    if (length < 0) break;
                    byte[] data = new byte[length];
                    in.readFully(data);

                    System.out.println("Received: " + new String(data));

                    String response = "Processed: " + new String(data);
                    out.writeInt(response.length());
                    out.write(response.getBytes());
                }
            } finally {
                clientSocket.close();
            }
        }
    }
}
