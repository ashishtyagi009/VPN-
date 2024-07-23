import java.io.*;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class VPNServer {
    private static final String KEY = "SixteenByteKey16"; // 16-byte AES key
    private static final String IV = "1234567890123456"; // 16-byte AES IV
    private static final int LOCAL_PORT = 8888; // Port for local service

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("VPN Server listening on port 9999");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Accepted connection from " + clientSocket.getInetAddress());

            new Thread(() -> {
                try {
                    handleClient(clientSocket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private static void handleClient(Socket clientSocket) throws Exception {
        DataInputStream in = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

        Socket localServiceSocket = new Socket("localhost", LOCAL_PORT);
        DataInputStream localServiceIn = new DataInputStream(localServiceSocket.getInputStream());
        DataOutputStream localServiceOut = new DataOutputStream(localServiceSocket.getOutputStream());

        try {
            while (true) {
                int length = in.readInt();
                if (length < 0) break;
                byte[] encryptedData = new byte[length];
                in.readFully(encryptedData);

                byte[] decryptedData = decrypt(encryptedData);
                System.out.println("Forwarding: " + new String(decryptedData));

                // Forward decrypted data to local service
                localServiceOut.writeInt(decryptedData.length);
                localServiceOut.write(decryptedData);

                // Optionally, read response from local service
                int responseLength = localServiceIn.readInt();
                byte[] responseData = new byte[responseLength];
                localServiceIn.readFully(responseData);

                byte[] encryptedResponse = encrypt(responseData);
                out.writeInt(encryptedResponse.length);
                out.write(encryptedResponse);
            }
        } finally {
            clientSocket.close();
            localServiceSocket.close();
        }
    }

    private static byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(data);
    }

    private static byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        return cipher.doFinal(data);
    }
}
