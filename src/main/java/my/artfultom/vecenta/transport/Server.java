package my.artfultom.vecenta.transport;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Server {
    private AsynchronousServerSocketChannel listener = null;
    private long timeout = 1;

    private Map<String, MethodHandler> handlerMap = new HashMap<>();

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
                                if (count == 0) {
                                    break;
                                }

                                while (listener.isOpen() && byteBuffer.position() < cursor + 4 + count) {
                                    int bytesRead = ch.read(byteBuffer).get(timeout, TimeUnit.SECONDS);
                                    if (bytesRead == -1) {
                                        break;
                                    }
                                }

                                if (!listener.isOpen()) {
                                    break;
                                }

                                byte[] req = Arrays.copyOfRange(byteBuffer.array(), cursor + 4, cursor + 4 + count);
                                byte[] resp = process(req);

                                ByteArrayOutputStream out = new ByteArrayOutputStream(resp.length + 4);
                                DataOutputStream dataStream = new DataOutputStream(out);
                                try {
                                    dataStream.writeInt(resp.length);
                                    dataStream.write(resp);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ch.write(ByteBuffer.wrap(out.toByteArray()));

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

    public void register(MethodHandler handler) {
        if (handlerMap.containsKey(handler.getName())) {
            // TODO ругнуться
            return;
        }

        handlerMap.put(handler.getName(), handler);
    }

    private byte[] process(byte[] in) {
        ByteBuffer buf = ByteBuffer.wrap(in);

        int methodSize = 0;
        try {
            methodSize = buf.getInt(0);
        } catch (Exception e) {
            System.out.println("!!!");
        }


        byte[] rawMethod = Arrays.copyOfRange(in, 4, methodSize + 4);
        String method = new String(rawMethod, StandardCharsets.UTF_8);

        List<String> params = new ArrayList<>();
        for (int i = methodSize + 4; i < buf.capacity(); ) {
            byte[] rawSize = Arrays.copyOfRange(in, i, i + 4);
            int paramSize = ByteBuffer.wrap(rawSize).getInt();

            byte[] rawParam = Arrays.copyOfRange(in, i + 4, paramSize + i + 4);

            params.add(new String(rawParam, StandardCharsets.UTF_8));

            i += paramSize + 4;
        }

        MethodHandler handler = handlerMap.get(method);
        if (handler == null) {
            // TODO ругнуться
        }

        Response response = handler.execute(new Request(method, params));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(out);

        try {
            dataStream.writeByte(0);
            for (String param : response.getParams()) {
                dataStream.writeInt(param.length());
                dataStream.writeBytes(param);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
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
