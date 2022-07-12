package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.Configuration;
import io.github.artfultom.vecenta.matcher.DefaultReadWriteStrategy;
import io.github.artfultom.vecenta.matcher.ReadWriteStrategy;
import io.github.artfultom.vecenta.transport.AbstractClient;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class TcpClient extends AbstractClient {

    private static final Logger log = LoggerFactory.getLogger(TcpClient.class);
    private static final int SEND_ATTEMPT_COUNT = Configuration.getInt("send.attempt_count");

    private String host;
    private int port;

    private Socket clientSocket;
    private DataOutputStream out;
    private DataInputStream in;

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
            log.error("IO error during connection to " + host + ":" + port, e);
        }
    }

    @Override
    public Response send(Request request) throws ConnectException {
        for (int i = 0; i < SEND_ATTEMPT_COUNT; i++) {
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
                log.info(String.format("Reconnecting to %s:%d", host, port));
                connect();
            } catch (IOException e) {
                log.error("IO error during sending message to " + host + ":" + port, e);
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
