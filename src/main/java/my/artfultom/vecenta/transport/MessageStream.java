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
    private final long TIMEOUT = 1;

    private ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
    private int cursor = 0;

    public MessageStream(AsynchronousSocketChannel channel) {
        this.channel = channel;
    }

    public byte[] getNextMessage() {
        int size;

        try {
            while (byteBuffer.position() < cursor + 4) {
                int bytesRead = channel.read(byteBuffer).get(TIMEOUT, TimeUnit.SECONDS);
                if (bytesRead == -1) {
                    return null;
                }
            }

            size = byteBuffer.getInt(cursor);
            if (size == 0) {
                return null;
            }

            while (byteBuffer.position() < cursor + 4 + size) {
                int bytesRead = channel.read(byteBuffer).get(TIMEOUT, TimeUnit.SECONDS);
                if (bytesRead == -1) {
                    return null;
                }
            }

            byte[] message = Arrays.copyOfRange(byteBuffer.array(), cursor + 4, cursor + 4 + size);

            if (cursor > byteBuffer.capacity() * 0.8) {
                for (int i = 0, j = byteBuffer.position(); j < byteBuffer.capacity(); i++, j++) {
                    byteBuffer.put(i, byteBuffer.get(j));
                    byteBuffer.clear();
                    cursor = 0;
                }
            } else {
                cursor += size + 4;
            }

            return message;
        } catch (InterruptedException ignored) {
        } catch (ExecutionException | TimeoutException e) {
            cursor = 0;
            byteBuffer.clear();

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
