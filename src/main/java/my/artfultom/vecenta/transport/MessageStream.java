package my.artfultom.vecenta.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MessageStream {
    private AsynchronousSocketChannel channel;
    private final long timeout;

    public MessageStream(AsynchronousSocketChannel channel, long timeout) {
        this.channel = channel;
        this.timeout = timeout;
    }

    public byte[] getNextMessage() {
        int size;
        ByteBuffer sizeBuf = ByteBuffer.allocate(4);

        try {
            while (sizeBuf.position() < 4) {
                int bytesRead = channel.read(sizeBuf).get(timeout, TimeUnit.MILLISECONDS);
                if (bytesRead == -1) {
                    return null;
                }
            }

            size = sizeBuf.getInt(0);
            if (size == 0) {
                return null;
            }

            ByteBuffer messageBuf = ByteBuffer.allocate(size);

            while (messageBuf.position() < messageBuf.capacity()) {
                int bytesRead = channel.read(messageBuf).get(timeout, TimeUnit.MILLISECONDS);
                if (bytesRead == -1) {
                    return null;
                }
            }

            return messageBuf.array();
        } catch (InterruptedException ignored) {
        } catch (ExecutionException | TimeoutException e) {
            try {
                if (channel.isOpen()) {
                    channel.shutdownInput();
                    channel.shutdownOutput();
                    channel.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    public void sendMessage(byte[] resp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(resp.length + 4);
        DataOutputStream dataStream = new DataOutputStream(out);
        try {
            dataStream.writeInt(resp.length);
            dataStream.write(resp);
        } catch (IOException e) {
            e.printStackTrace();
        }

        channel.write(ByteBuffer.wrap(out.toByteArray()));
    }
}
