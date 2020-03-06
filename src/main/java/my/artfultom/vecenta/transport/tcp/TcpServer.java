package my.artfultom.vecenta.transport.tcp;

import my.artfultom.vecenta.transport.ConvertStrategy;
import my.artfultom.vecenta.transport.DefaultConvertStrategy;
import my.artfultom.vecenta.transport.MethodHandler;
import my.artfultom.vecenta.transport.Server;
import my.artfultom.vecenta.transport.message.MessageStream;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

public class TcpServer implements Server {
    private AsynchronousServerSocketChannel listener = null;
    private Map<String, MethodHandler> handlerMap = new HashMap<>(); // TODO here?

    private long timeout = 5000;

    private ConvertStrategy strategy;

    public TcpServer() {
        this.strategy = new DefaultConvertStrategy();
    }

    public TcpServer(ConvertStrategy strategy) {
        this.strategy = strategy;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
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

                    // TODO handshake

                    MessageStream stream = new MessageStream(ch, timeout);

                    while (listener.isOpen()) {
                        byte[] req = stream.getNextMessage();
                        if (req == null) {
                            break;
                        }

                        byte[] resp = process(req);

                        stream.sendMessage(resp);
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
    public boolean register(MethodHandler handler) {
        if (handlerMap.containsKey(handler.getName())) {
            return false;
        }

        handlerMap.put(handler.getName(), handler);
        return true;
    }

    private byte[] process(byte[] in) {
        Request request = strategy.convertToRequest(in);

        MethodHandler handler = handlerMap.get(request.getMethodName());
        if (handler == null) {
            return strategy.convertToBytes(new Response(666));
        }

        Response response = handler.execute(request);

        return strategy.convertToBytes(response);
    }

    @Override
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
