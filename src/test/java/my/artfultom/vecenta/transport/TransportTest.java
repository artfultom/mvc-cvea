package my.artfultom.vecenta.transport;

import my.artfultom.vecenta.matcher.ServerMatcher;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;
import my.artfultom.vecenta.transport.tcp.TcpClient;
import my.artfultom.vecenta.transport.tcp.TcpServer;
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
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams()))); // TODO one or many?

        try (Server server = new TcpServer()) {
            server.start(5550, matcher);

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try (TcpClient client = new TcpClient()) {
                        client.startConnection("127.0.0.1", 5550);

                        for (int j = 0; j < 100; j++) {
                            byte[] param1 = ("param1" + j).getBytes();
                            byte[] param2 = ("param2" + j).getBytes();
                            Response resp = client.send(new Request("echo", List.of(param1, param2)));

                            Assert.assertEquals(2, resp.getResults().size());
                            Assert.assertArrayEquals(param1, resp.getResults().get(0));
                            Assert.assertArrayEquals(param2, resp.getResults().get(1));
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
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams()))); // TODO one or many?

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

                            Assert.assertEquals(1, resp.getResults().size());
                            Assert.assertArrayEquals(param, resp.getResults().get(0));

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
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams()))); // TODO one or many?

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
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams()))); // TODO one or many?

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
            Assert.assertNotNull(response.getErrorCode());
            Assert.assertNull(response.getResults());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void error2Handler() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(new MethodHandler("echo", (request) -> new Response(request.getParams()))); // TODO one or many?

        try (TcpServer server = new TcpServer(); Client client = new TcpClient()) {
            server.start(5550, matcher);
            client.startConnection("127.0.0.1", 5550);

            Response response = client.send(new Request("wrong", new ArrayList<>()));

            Assert.assertNotNull(response);
            Assert.assertNotNull(response.getErrorCode());
            Assert.assertNull(response.getResults());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}