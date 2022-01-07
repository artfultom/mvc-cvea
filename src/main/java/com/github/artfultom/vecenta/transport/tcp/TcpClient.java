package com.github.artfultom.vecenta.transport.tcp;

import com.github.artfultom.vecenta.matcher.DefaultReadWriteStrategy;
import com.github.artfultom.vecenta.matcher.ReadWriteStrategy;
import com.github.artfultom.vecenta.transport.AbstractClient;
import com.github.artfultom.vecenta.transport.message.Request;
import com.github.artfultom.vecenta.transport.message.Response;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class TcpClient extends AbstractClient {

    private String host;
    private int port;

    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

    private ReadWriteStrategy strategy; // TODO here?

    public TcpClient() {
        this.strategy = new DefaultReadWriteStrategy();
    }

    public TcpClient(ReadWriteStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void startConnection(String host, int port) throws ConnectException {
        this.host = host;
        this.port = port;

        connect();
    }

    private void connect() throws ConnectException {
        try {
            clientSocket = new Socket(host, port);

            out = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
            in = new DataInputStream(clientSocket.getInputStream());

            handshake(in, out);
        } catch (ConnectException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response send(Request request) throws ConnectException {
        for (int i = 0; i < 10; i++) {  // TODO settings
            try {
                byte[] b = strategy.convertToBytes(request);
                out.writeInt(b.length);
                out.write(b);
                out.flush();

                int size = in.readInt();
                byte[] result = in.readNBytes(size);
                return strategy.convertToResponse(result);
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

    @Override
    public void close() throws IOException {
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
