import java.io.*;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class VPNClient {
    private static final String KEY = "SixteenByteKey16"; // 16-byte AES key
    private static final String IV = "1234567890123456"; // 16-byte AES IV

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket("127.0.0.1", 9999);
        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (true) {
                System.out.print("Enter message: ");
                String message = reader.readLine();

                byte[] encryptedData = encrypt(message.getBytes());

                out.writeInt(encryptedData.length);
                out.write(encryptedData);

                int length = in.readInt();
                byte[] encryptedResponse = new byte[length];
                in.readFully(encryptedResponse);

                byte[] decryptedResponse = decrypt(encryptedResponse);
                System.out.println("Received: " + new String(decryptedResponse));
            }
        } finally {
            socket.close();
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
