package io.github.artfultom.vecenta.transport.tcp;

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

public class TcpMessageStream implements MessageStream {

    private static final Logger log = LoggerFactory.getLogger(TcpMessageStream.class);

    private final AsynchronousSocketChannel channel;
    private final long timeout;

    public TcpMessageStream(AsynchronousSocketChannel channel, long timeout) {
        this.channel = channel;
        this.timeout = timeout;
    }

    @Override
    public byte[] getMessage() {
        ByteBuffer sizeBuf = ByteBuffer.allocate(Integer.BYTES);

        try {
            while (sizeBuf.position() < Integer.BYTES) {
                int bytesRead = channel.read(sizeBuf).get(timeout, TimeUnit.MILLISECONDS);
                if (bytesRead == -1) {
                    return new byte[0];
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
                    return new byte[0];
                }
            }

            return messageBuf.array();
        } catch (InterruptedException e) {
            log.error("Getting data from channel was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            try {
                if (channel.isOpen()) {
                    channel.shutdownInput();
                    channel.shutdownOutput();
                    channel.close();
                }
            } catch (IOException ex) {
                log.error("Cannot close the channel", e);
            }
        }

        return new byte[0];
    }

    @Override
    public void sendMessage(byte[] resp) {
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream(resp.length + Integer.BYTES);
                DataOutputStream dataStream = new DataOutputStream(out)
        ) {
            dataStream.writeInt(resp.length);
            dataStream.write(resp);

            channel.write(ByteBuffer.wrap(out.toByteArray()));
        } catch (IOException e) {
            log.error("Cannot send the message", e);
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}
