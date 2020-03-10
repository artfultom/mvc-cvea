package my.artfultom.vecenta.transport.tcp;

import my.artfultom.vecenta.matcher.ServerMatcher;
import my.artfultom.vecenta.transport.Server;
import my.artfultom.vecenta.transport.message.MessageStream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TcpServer implements Server {

    private final int PROTOCOL_VERSION = 1;

    private AsynchronousServerSocketChannel listener;

    private long timeout = 5000;

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void start(int port, ServerMatcher matcher) {
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

                    try (MessageStream stream = new MessageStream(ch, timeout)) {
                        while (listener.isOpen()) {
                            byte[] req = stream.getNextMessage();
                            if (req == null) {
                                break;
                            }

                            byte[] resp = matcher.process(req);

                            stream.sendMessage(resp);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
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

    @Override
    public void close() throws IOException {
        if (listener.isOpen()) {
            listener.close();
        }
    }
}
