package my.artfultom.vecenta.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Client {
    private String host;
    private int port;

    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    public void startConnection(String host, int port) throws ConnectException {
        this.host = host;
        this.port = port;

        connect();
    }

    private void connect() throws ConnectException {
        try {
            clientSocket = new Socket(host, port);
            out = new DataOutputStream(clientSocket.getOutputStream());
            in = new DataInputStream(clientSocket.getInputStream());
        } catch (ConnectException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendMessage(String msg) throws ConnectException {
        for (int i = 0; i < 10; i++) {
            try {
                byte[] b = msg.getBytes(StandardCharsets.UTF_8);
                out.writeInt(b.length);
                out.write(b);

                int size = in.readInt();
                byte[] result = in.readNBytes(size);
                return new String(result, StandardCharsets.UTF_8);
            } catch (ConnectException e) {
                throw e;
            } catch (SocketException | EOFException e) {
                connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void stopConnection() throws IOException {
        if (clientSocket != null && !clientSocket.isClosed()) {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }

            clientSocket.close();
        }
    }
}
