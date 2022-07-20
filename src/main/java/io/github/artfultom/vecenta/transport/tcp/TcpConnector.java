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
                        "Cannot connect to " + host + ":" + port,
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
            if (client != null) {
                client.close();
            }
        } catch (IOException ex) {
            throw new ConnectionException("Cannot close the connector.", ex);
        }
    }
}
