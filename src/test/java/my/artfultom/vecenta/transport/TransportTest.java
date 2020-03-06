package my.artfultom.vecenta.transport;

import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;
import my.artfultom.vecenta.transport.tcp.TcpClient;
import my.artfultom.vecenta.transport.tcp.TcpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TransportTest {

    @Test
    public void manyClients() {
        Server server = new TcpServer();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.start(5550);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    TcpClient client = new TcpClient();
                    client.startConnection("127.0.0.1", 5550);
                    for (int j = 0; j < 100; j++) {
                        byte[] param1 = ("param1" + j).getBytes();
                        byte[] param2 = ("param2" + j).getBytes();
                        Response resp = client.send(new Request("echo", List.of(param1, param2)));

                        Assert.assertEquals(2, resp.getParams().size());
                        Assert.assertArrayEquals(param1, resp.getParams().get(0));
                        Assert.assertArrayEquals(param2, resp.getParams().get(1));
                    }
                    client.stopConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            futures.add(future);
        }

        for (CompletableFuture<Void> future : futures) {
            future.join();
        }

        server.stop();
    }

    @Test
    public void timeoutClients() {
        TcpServer server = new TcpServer();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.setTimeout(100);
        server.start(5550);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Client client = new TcpClient();
                    client.startConnection("127.0.0.1", 5550);

                    for (int j = 0; j < 5; j++) {
                        byte[] param = ("param" + j).getBytes();
                        Response resp = client.send(new Request("echo", List.of(param)));

                        Assert.assertEquals(1, resp.getParams().size());
                        Assert.assertArrayEquals(param, resp.getParams().get(0));

                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    client.stopConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            futures.add(future);
        }

        for (CompletableFuture<Void> future : futures) {
            future.join();
        }

        server.stop();
    }

    @Test
    public void error1Clients() {
        Server server = new TcpServer();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.start(5550);

        server.stop();

        for (int i = 0; i < 10; i++) {
            try {
                Client client = new TcpClient();
                client.startConnection("127.0.0.1", 5550);

                Assert.fail();
            } catch (ConnectException ignored) {
            }
        }
    }

    @Test
    public void error2Clients() {
        Server server = new TcpServer();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.start(5550);

        try {
            Client client = new TcpClient();
            client.startConnection("127.0.0.1", 5550);

            server.stop();

            client.send(new Request("echo", new ArrayList<>()));

            Assert.fail();
        } catch (ConnectException ignored) {
        }
    }

    @Test
    public void error1Handler() throws ConnectException {
        Server server = new TcpServer();
        server.start(5550);

        Client client = new TcpClient();
        client.startConnection("127.0.0.1", 5550);

        Response response = client.send(new Request("echo", new ArrayList<>()));

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getErrorCode());
        Assert.assertNull(response.getParams());

        server.stop();
    }

    @Test
    public void error2Handler() throws ConnectException {
        Server server = new TcpServer();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.start(5550);

        Client client = new TcpClient();
        client.startConnection("127.0.0.1", 5550);

        Response response = client.send(new Request("wrong", new ArrayList<>()));

        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getErrorCode());
        Assert.assertNull(response.getParams());

        server.stop();
    }
}