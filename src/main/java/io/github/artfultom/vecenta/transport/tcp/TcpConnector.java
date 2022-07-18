package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.Configuration;
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
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TcpConnector extends AbstractConnector {

    private static final Logger log = LoggerFactory.getLogger(TcpConnector.class);
    private static final int SEND_ATTEMPT_COUNT = Configuration.getInt("send.attempt_count");
    private int timeout = Configuration.getInt("client.default_timeout");   // TODO from handshake

    private String host;
    private int port;

    private AsynchronousSocketChannel client;

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
            client = AsynchronousSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress(host, port);

            client
                    .connect(address)
                    .get(timeout, TimeUnit.MILLISECONDS);

            stream = new TcpMessageStream(client, timeout);
            handshake(stream);
        } catch (IOException e) {
            log.error("IO error during connection to " + host + ":" + port, e);
        } catch (ExecutionException | TimeoutException e) {
            if (e.getCause() instanceof ConnectException) {
                throw new ConnectionException(
                        "Connot connect to " + host + ":" + port,
                        (ConnectException) e.getCause()
                );
            }

            log.error("IO error during connection to " + host + ":" + port, e);
        } catch (InterruptedException e) {
            log.error("IO error during connection to " + host + ":" + port, e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized Response send(Request request) throws ConnectionException {
        byte[] b = strategy.convertToBytes(request);

        for (int i = 0; i < SEND_ATTEMPT_COUNT; i++) {
            try {
                stream.sendMessage(b);

                for (int j = 0; j < SEND_ATTEMPT_COUNT; j++) {
                    try {
                        byte[] readResult = stream.getMessage();
                        return strategy.convertToResponse(readResult);
                    } catch (ConnectionException ex) {
                        log.warn(ex.getMessage(), ex);
                    }
                }
            } catch (ConnectionException ex) {
                log.warn(ex.getMessage(), ex);
            }
        }

        ConnectionException ex = new ConnectionException("Cannot send the message.");
        log.error(ex.getMessage(), ex);
        throw ex;
    }

    @Override
    public void close() throws ConnectionException {
        try {
            if (stream != null) {
                stream.close();
            }
            if (client != null) {
                client.close();
            }
        } catch (IOException ex) {
            throw new ConnectionException("Cannot close the connector.", ex);
        }
    }
}
