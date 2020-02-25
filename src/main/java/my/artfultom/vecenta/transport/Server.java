package my.artfultom.vecenta.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Server {
    private AsynchronousServerSocketChannel listener = null;
    private long timeout = 1;

    public void start(int port) {
        try {
            listener = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(port));

            listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

                @Override
                public void completed(AsynchronousSocketChannel ch, Void att) {
                    if (listener.isOpen()) {
                        listener.accept(null, this);
                    } else {
                        return;
                    }

                    int bufferSize = 4096;
                    ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);

                    while (listener.isOpen()) {
                        try {
                            int cursor = 0;

                            while (byteBuffer.position() != cursor + 4) {
                                while (listener.isOpen() && byteBuffer.position() < cursor + 4) {
                                    int bytesRead = ch.read(byteBuffer).get(timeout, TimeUnit.SECONDS);
                                    if (bytesRead == -1) {
                                        break;
                                    }
                                }

                                int count = byteBuffer.getInt(cursor);

                                while (listener.isOpen() && byteBuffer.position() < cursor + 4 + count) {
                                    int bytesRead = ch.read(byteBuffer).get(timeout, TimeUnit.SECONDS);
                                    if (bytesRead == -1) {
                                        break;
                                    }
                                }

                                if (!listener.isOpen()) {
                                    return;
                                }

                                // logic
                                byte[] resp = Arrays.copyOfRange(byteBuffer.array(), cursor, cursor + 4 + count);
                                ch.write(ByteBuffer.wrap(resp));

                                cursor += count + 4;

                                if (cursor > bufferSize * 0.8) {
                                    for (int i = 0, j = byteBuffer.position(); j < byteBuffer.capacity(); i++, j++) {
                                        byteBuffer.put(i, byteBuffer.get(j));
                                        byteBuffer.clear();
                                        cursor = 0;
                                    }
                                }
                            }

                            byteBuffer.clear();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            break;
                        } catch (TimeoutException e) {
                            break;
                        } finally {
                            try {
                                if (ch.isOpen()) {
                                    ch.shutdownInput();
                                    ch.shutdownOutput();
                                    ch.close();
                                }
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void failed(Throwable e, Void att) {
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            if (listener.isOpen()) {
                listener.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
