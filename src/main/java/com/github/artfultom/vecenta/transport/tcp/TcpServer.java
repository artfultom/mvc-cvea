package com.github.artfultom.vecenta.transport.tcp;

import com.github.artfultom.vecenta.Configuration;
import com.github.artfultom.vecenta.matcher.ServerMatcher;
import com.github.artfultom.vecenta.transport.AbstractServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class TcpServer extends AbstractServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private AsynchronousServerSocketChannel listener;

    private long timeout = Configuration.getInt("server.default_timeout");

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

                    try (TcpMessageStream stream = new TcpMessageStream(ch, timeout)) {
                        if (listener.isOpen()) {
                            handshake(stream);
                        }

                        while (listener.isOpen()) {
                            byte[] req = stream.getNextMessage();
                            if (req == null || !listener.isOpen()) {
                                break;
                            }

                            byte[] resp = matcher.process(req);

                            stream.sendMessage(resp);
                        }
                    } catch (IOException e) {
                        log.error("stream error", e);
                    }
                }

                @Override
                public void failed(Throwable e, Void att) {
                }
            });
        } catch (IOException e) {
            log.error("cannot open asynchronous server socket channel", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (listener.isOpen()) {
            listener.close();
        }
    }
}
