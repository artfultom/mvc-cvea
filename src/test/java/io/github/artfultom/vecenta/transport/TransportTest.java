package io.github.artfultom.vecenta.transport;

import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.transport.tcp.TcpClient;
import io.github.artfultom.vecenta.transport.tcp.TcpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransportTest {

    @Test
    public void manyClients() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams().get(0))));

        try (Server server = new TcpServer()) {
            server.start(5550, matcher);

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try (TcpClient client = new TcpClient()) {
                        client.startConnection("127.0.0.1", 5550);

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
    public void timeoutClients() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams().get(0))));

        try (TcpServer server = new TcpServer()) {
            server.setTimeout(100);
            server.start(5550, matcher);

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 1; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try (Client client = new TcpClient()) {
                        client.startConnection("127.0.0.1", 5550);

                        for (int j = 0; j < 5; j++) {
                            byte[] param = ("param" + j).getBytes();
                            Response resp = client.send(new Request("echo", List.of(param)));

                            Assert.assertNotNull(resp.getResult());
                            Assert.assertArrayEquals(param, resp.getResult());

                            try {
                                Thread.sleep(150);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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
    public void error1Clients() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams().get(0))));

        try (TcpServer server = new TcpServer()) {
            server.start(5550, matcher);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            try (Client client = new TcpClient()) {
                client.startConnection("127.0.0.1", 5550);

                Assert.fail();
            } catch (IOException ignored) {
            }
        }
    }

    @Test
    public void error2Clients() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams().get(0))));

        TcpServer server = new TcpServer();
        server.start(5550, matcher);

        try (Client client = new TcpClient()) {
            client.startConnection("127.0.0.1", 5550);

            server.close();

            client.send(new Request("echo", new ArrayList<>()));

            Assert.fail();
        } catch (IOException ignored) {
        }
    }

    @Test
    public void error1Handler() {
        ServerMatcher matcher = new ServerMatcher();

        try (TcpServer server = new TcpServer(); Client client = new TcpClient()) {
            server.start(5550, matcher);
            client.startConnection("127.0.0.1", 5550);

            Response response = client.send(new Request("echo", new ArrayList<>()));

            Assert.assertNotNull(response);
            Assert.assertNotNull(response.getError());
            Assert.assertNull(response.getResult());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void error2Handler() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams().get(0))));

        try (TcpServer server = new TcpServer(); Client client = new TcpClient()) {
            server.start(5550, matcher);
            client.startConnection("127.0.0.1", 5550);

            Response response = client.send(new Request("wrong", new ArrayList<>()));

            Assert.assertNotNull(response);
            Assert.assertNotNull(response.getError());
            Assert.assertNull(response.getResult());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void manyResults() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("double", (request) -> {
            request.getParams().addAll(request.getParams());
            return new Response(request.getParams().get(0));
        }));

        try (TcpServer server = new TcpServer(); Client client = new TcpClient()) {
            server.start(5550, matcher);
            client.startConnection("127.0.0.1", 5550);

            Response response = client.send(new Request("double", List.of(new byte[]{1})));

            Assert.assertNotNull(response);
            Assert.assertNull(response.getError());
            Assert.assertNotNull(response.getResult());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}