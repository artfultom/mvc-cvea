package my.artfultom.vecenta.transport;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class TransportTest {

    @Test
    public void manyClients() {
        Server server = new Server();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.start(5550);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Client client = new Client();
                    client.startConnection("127.0.0.1", 5550);
                    for (int j = 0; j < 100; j++) {
                        String param1 = "param1" + j;
                        String param2 = "param2" + j;
                        Response resp = client.send(new Request("echo", List.of(param1, param2)));

                        Assert.assertEquals(2, resp.getParams().size());
                        Assert.assertEquals(param1, resp.getParams().get(0));
                        Assert.assertEquals(param2, resp.getParams().get(1));
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
        Server server = new Server();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.start(5551);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Client client = new Client();
                    client.startConnection("127.0.0.1", 5551);

                    for (int j = 0; j < 3; j++) {
                        String param = "param" + j;
                        Response resp = client.send(new Request("echo", List.of(param)));

                        Assert.assertEquals(1, resp.getParams().size());
                        Assert.assertEquals(param, resp.getParams().get(0));

                        try {
                            Thread.sleep(1500);
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
        Server server = new Server();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.start(5552);

        server.stop();

        for (int i = 0; i < 10; i++) {
            try {
                Client client = new Client();
                client.startConnection("127.0.0.1", 5552);

                Assert.fail();
            } catch (ConnectException ignored) {
            }
        }
    }

    @Test
    public void error2Clients() {
        Server server = new Server();
        MethodHandler handler = new MethodHandler("echo", (request) -> new Response(request.getParams()));
        server.register(handler);
        server.start(5553);

        try {
            Client client = new Client();
            client.startConnection("127.0.0.1", 5553);

            server.stop();

            client.send(new Request("echo", new ArrayList<>()));

            Assert.fail();
        } catch (ConnectException ignored) {
        }
    }
}