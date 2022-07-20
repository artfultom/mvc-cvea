package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.transport.MessageStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TcpAsyncMessageStream implements MessageStream {

    private static final Logger log = LoggerFactory.getLogger(TcpAsyncMessageStream.class);

    private final AsynchronousSocketChannel channel;
    private final long timeout;

    public TcpAsyncMessageStream(AsynchronousSocketChannel channel, long timeout) {
        this.channel = channel;
        this.timeout = timeout;
    }

    @Override
    public byte[] getMessage() throws ConnectionException {
        ByteBuffer sizeBuf = ByteBuffer.allocate(Integer.BYTES);

        try {
            while (sizeBuf.position() < Integer.BYTES) {
                int bytesRead = channel.read(sizeBuf).get(timeout, TimeUnit.MILLISECONDS);
                if (bytesRead == -1) {
                    log.error("Connection is closed");
                    throw new ConnectionException("Connection is closed");
                }
            }

            int size = sizeBuf.getInt(0);
            if (size == 0) {
                return new byte[0];
            }

            ByteBuffer messageBuf = ByteBuffer.allocate(size);

            while (messageBuf.position() < messageBuf.capacity()) {
                int bytesRead = channel.read(messageBuf).get(timeout, TimeUnit.MILLISECONDS);
                if (bytesRead == -1) {
                    log.error("Connection is closed");
                    throw new ConnectionException("Connection is closed");
                }
            }

            return messageBuf.array();
        } catch (InterruptedException e) {
            log.error("Getting data from channel was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            log.error("Cannot get a message.");
            throw new ConnectionException("Cannot get a message.", e);
        }

        return new byte[0];
    }

    @Override
    public void sendMessage(byte[] resp) throws ConnectionException {
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream(resp.length + Integer.BYTES);
                DataOutputStream dataStream = new DataOutputStream(out)
        ) {
            dataStream.writeInt(resp.length);
            dataStream.write(resp);

            int result = channel
                    .write(ByteBuffer.wrap(out.toByteArray()))
                    .get(timeout, TimeUnit.MILLISECONDS);

            if (result == -1) {
                throw new ConnectionException("Connection is closed");
            }
        } catch (IOException | ExecutionException | TimeoutException e) {
            log.error("Cannot send the message", e);
        } catch (InterruptedException e) {
            log.error("Cannot send the message", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() throws IOException {
        if (channel.isOpen()) {
            channel.shutdownInput();
            channel.shutdownOutput();
            channel.close();
        }
    }
}
