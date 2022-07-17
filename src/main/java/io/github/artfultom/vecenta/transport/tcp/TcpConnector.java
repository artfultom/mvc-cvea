package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.Configuration;
import io.github.artfultom.vecenta.matcher.DefaultReadWriteStrategy;
import io.github.artfultom.vecenta.matcher.ReadWriteStrategy;
import io.github.artfultom.vecenta.matcher.TypeConverter;
import io.github.artfultom.vecenta.transport.AbstractConnector;
import io.github.artfultom.vecenta.transport.MessageStream;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
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
    public void connect(String host, int port) throws ConnectException {
        this.host = host;
        this.port = port;

        connect();
    }

    private synchronized void connect() throws ConnectException {
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
                throw (ConnectException) e.getCause();
            }

            log.error("IO error during connection to " + host + ":" + port, e);
        } catch (InterruptedException e) {
            log.error("IO error during connection to " + host + ":" + port, e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public synchronized Response send(Request request) throws ConnectException {
        byte[] b = strategy.convertToBytes(request);

        ByteBuffer writeBuffer = ByteBuffer.allocate(Integer.BYTES + b.length);
        writeBuffer.put(TypeConverter.INTEGER.convert(b.length));
        writeBuffer.put(b);

        for (int i = 0; i < SEND_ATTEMPT_COUNT; i++) {
            try {
                client
                        .write(writeBuffer.position(0))
                        .get(timeout, TimeUnit.MILLISECONDS);

                ByteBuffer readSizeBuffer = ByteBuffer.allocate(Integer.BYTES);
                int sizeResult = client
                        .read(readSizeBuffer.position(0))
                        .get(timeout, TimeUnit.MILLISECONDS);
                if (sizeResult == -1) {
                    throw new RuntimeException("The server is closed"); // TODO
                }

                ByteBuffer readBuffer = ByteBuffer.allocate(readSizeBuffer.getInt(0));
                int readResult = client
                        .read(readBuffer.position(0))
                        .get(timeout, TimeUnit.MILLISECONDS);
                if (readResult == -1) {
                    throw new RuntimeException("The server is closed"); // TODO
                }

                return strategy.convertToResponse(readBuffer.array());
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                log.info(String.format("Reconnecting to %s:%d", host, port));
                connect();
            }
        }

        return null;
    }

    @Override
    public void close() throws IOException {
        if (stream != null) {
            stream.close();
        }
        if (client != null) {
            client.close();
        }
    }
}
