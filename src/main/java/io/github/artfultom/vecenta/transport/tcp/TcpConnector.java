package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.matcher.DefaultReadWriteStrategy;
import io.github.artfultom.vecenta.matcher.ReadWriteStrategy;
import io.github.artfultom.vecenta.transport.AbstractConnector;
import io.github.artfultom.vecenta.transport.MessageStream;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TcpConnector extends AbstractConnector {

    private static final Logger log = LoggerFactory.getLogger(TcpConnector.class);

    private String host;
    private int port;

    private Socket socket;

    private MessageStream stream;

    public TcpConnector() {
        this.strategy = new DefaultReadWriteStrategy();
    }

    public TcpConnector(ReadWriteStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public void connect(String host, int port) throws ConnectionException {
        this.host = host;
        this.port = port;

        connect();
    }

    private synchronized void connect() throws ConnectionException {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port));

            stream = new TcpMessageStream(socket);
        } catch (IOException e) {
            ConnectionException ex = new ConnectionException("IO error during connection to " + host + ":" + port);
            log.error(ex.getMessage(), e);
            throw ex;
        }

        handshake(stream);
    }

    @Override
    public synchronized Response send(Request request) throws ConnectionException {
        byte[] b = strategy.convertToBytes(request);

        stream.sendMessage(b);

        byte[] readResult = stream.getMessage();
        return strategy.convertToResponse(readResult);
    }

    @Override
    public void close() throws ConnectionException {
        try {
            if (stream != null) {
                stream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException ex) {
            throw new ConnectionException("Cannot close the connector.", ex);
        }
    }
}
