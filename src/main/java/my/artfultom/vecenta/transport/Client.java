package my.artfultom.vecenta.transport;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public Response send(Request request) throws ConnectException { // TODO выбор сериализации
        for (int i = 0; i < 10; i++) {
            try {
                byte[] b = convertToBytes(request);
                out.writeInt(b.length);
                out.write(b);

                int size = in.readInt();
                byte[] result = in.readNBytes(size);
                return convertToResponse(result);
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

    private byte[] convertToBytes(Request in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(out);

        try {
            int methodLength = in.getMethodName().length();
            dataStream.writeInt(methodLength);
            dataStream.writeBytes(in.getMethodName());

            for (byte[] param : in.getParams()) {
                int paramLength = param.length;
                dataStream.writeInt(paramLength);
                dataStream.write(param);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private Response convertToResponse(byte[] in) {
        ByteBuffer buf = ByteBuffer.wrap(in);

        byte flag = buf.get(0);

        if (flag == 0) {
            List<byte[]> params = new ArrayList<>();

            for (int i = 1; i < buf.capacity(); ) {
                byte[] rawSize = Arrays.copyOfRange(in, i, i + 4);
                int size = ByteBuffer.wrap(rawSize).getInt();
                byte[] param = Arrays.copyOfRange(in, i + 4, i + 4 + size);

                params.add(param);

                i += size + 4;
            }

            return new Response(params);
        } else {
            return new Response(1); // TODO code
        }
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
