package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.Configuration;
import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.AbstractServer;
import io.github.artfultom.vecenta.transport.MessageStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TcpServer extends AbstractServer {

    private static final Logger log = LoggerFactory.getLogger(TcpServer.class);

    private ServerSocket listener;

    private long timeout = Configuration.getLong("server.default_timeout"); // TODO
    private static final long FIRST_CLIENT_ID = Configuration.getLong("server.first_client_id");    // TODO

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public void start(int port, ServerMatcher matcher) {
        try {
            listener = new ServerSocket();
            listener.bind(new InetSocketAddress(port));

            Executor executionPool = Executors.newCachedThreadPool();

            Executor acceptPool = Executors.newSingleThreadExecutor();
            acceptPool.execute(new Runnable() {

                @Override
                public void run() {
                    Socket socket;
                    try {
                        socket = listener.accept();
                    } catch (SocketException e) {
                        return;
                    } catch (IOException e) {
                        log.error("Cannot accept a socket.", e);
                        return;
                    }

                    if (!listener.isClosed()) {
                        acceptPool.execute(this);
                    }

                    executionPool.execute(() -> {
                        try (MessageStream stream = new TcpMessageStream(socket)) {
                            if (!socket.isClosed() && !listener.isClosed()) {
                                handshake(stream);
                            }

                            while (!socket.isClosed() && !listener.isClosed()) {
                                byte[] req = stream.getMessage();
                                if (req.length == 0) {
                                    continue;
                                }

                                byte[] resp = matcher.process(req);
                                stream.sendMessage(resp);
                            }
                        } catch (IOException e) {
                            log.error("Error in the MessageStream.", e);
                        } catch (ConnectionException e) {
                            log.error("Error in the handshake.", e);
                        } finally {
                            if (!socket.isClosed()) {
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    log.error("Cannot close the socket.", e);
                                }
                            }
                        }
                    });
                }
            });
        } catch (IOException e) {
            log.error("Cannot open a server socket.", e);
        }
    }

    @Override
    public void close() throws ConnectionException {
        try {
            if (!listener.isClosed()) {
                listener.close();
            }
        } catch (IOException e) {
            throw new ConnectionException("Cannot close the server.", e);
        }
    }
}
