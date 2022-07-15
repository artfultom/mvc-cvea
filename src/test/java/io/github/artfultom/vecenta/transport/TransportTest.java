package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.transport.tcp.TcpConnector;
import io.github.artfultom.vecenta.transport.tcp.TcpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class TransportTest {

    @Test
    public void manyClients() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", request -> new Response(request.getParams().get(0))));

        try (Server server = new TcpServer()) {
            server.start(5550, matcher);

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try (TcpConnector client = new TcpConnector()) {
                        client.connect("127.0.0.1", 5550);

                        for (int j = 0; j < 100; j++) {
                            byte[] param1 = ("param1" + j).getBytes();
                            Response resp = client.send(new Request("echo", List.of(param1)));

                            Assert.assertNotNull(resp.getResult());
                            Assert.assertArrayEquals(param1, resp.getResult());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

                futures.add(future);
            }

            for (CompletableFuture<Void> future : futures) {
                future.join();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void errorConnectionRefused() throws IOException {
        for (int i = 0; i < 10; i++) {
            try (Connector connector = new TcpConnector()) {
                Assert.assertThrows(
                        IOException.class,
                        () -> connector.connect("127.0.0.1", 5550)
                );
            }
        }
    }

    @Test
    public void errorServerClosed() throws IOException {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", request -> new Response(request.getParams().get(0))));

        TcpServer server = new TcpServer();
        server.start(5550, matcher);

        try (Connector connector = new TcpConnector()) {
            connector.connect("127.0.0.1", 5550);
            server.close();

            Assert.assertThrows(
                    IOException.class,
                    () -> connector.send(new Request("echo", new ArrayList<>()))
            );
        }
    }

    @Test
    public void error1Handler() throws IOException {
        ServerMatcher matcher = new ServerMatcher();

        try (TcpServer server = new TcpServer(); Connector connector = new TcpConnector()) {
            server.start(5550, matcher);
            connector.connect("127.0.0.1", 5550);

            Response response = connector.send(new Request("echo", new ArrayList<>()));

            Assert.assertNotNull(response);
            Assert.assertNotNull(response.getError());
            Assert.assertNull(response.getResult());
        }
    }

    @Test
    public void error2Handler() throws IOException {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", request -> new Response(request.getParams().get(0))));

        try (TcpServer server = new TcpServer(); Connector connector = new TcpConnector()) {
            server.start(5550, matcher);
            connector.connect("127.0.0.1", 5550);

            Response response = connector.send(new Request("wrong", new ArrayList<>()));

            Assert.assertNotNull(response);
            Assert.assertNotNull(response.getError());
            Assert.assertNull(response.getResult());
        }
    }

    @Test
    public void manyResults() throws IOException {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("inc", request -> {
            int val = ByteBuffer.wrap(request.getParams().get(0)).getInt();
            val++;

            return new Response(ByteBuffer.allocate(Integer.BYTES).putInt(val).array());
        }));

        try (
                TcpServer server = new TcpServer();
                Connector connector1 = new TcpConnector();
                Connector connector2 = new TcpConnector()
        ) {
            server.start(5550, matcher);
            connector1.connect("127.0.0.1", 5550);
            connector2.connect("127.0.0.1", 5550);

            IntStream.range(0, 1000).parallel()
                    .mapToObj(item -> CompletableFuture.runAsync(() -> {
                        try {
                            int val = new Random().nextInt();
                            Response response1 = connector1.send(new Request(
                                    "inc",
                                    List.of(ByteBuffer.allocate(Integer.BYTES).putInt(val).array())
                            ));

                            Assert.assertNotNull(response1);
                            Assert.assertNull(response1.getError());
                            Assert.assertNotNull(response1.getResult());
                            Assert.assertEquals(val + 1, ByteBuffer.wrap(response1.getResult()).getInt());

                            Response response2 = connector2.send(new Request(
                                    "inc",
                                    List.of(ByteBuffer.allocate(Integer.BYTES).putInt(val).array())
                            ));

                            Assert.assertNotNull(response2);
                            Assert.assertNull(response2.getError());
                            Assert.assertNotNull(response2.getResult());
                            Assert.assertEquals(val + 1, ByteBuffer.wrap(response2.getResult()).getInt());
                        } catch (ConnectException e) {
                            Assert.fail();
                        }
                    })).forEach(CompletableFuture::join);
        }
    }
}