package my.artfultom.vecenta.transport;

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
        Server server = new Server();
        server.start(5550);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Client client = new Client();
                    client.startConnection("127.0.0.1", 5550);
                    for (int j = 0; j < 100; j++) {
                        String msg = "test" + j;
                        String resp = client.sendMessage(msg);

                        Assert.assertEquals(msg, resp);
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
        server.start(5551);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Client client = new Client();
                    client.startConnection("127.0.0.1", 5551);

                    for (int j = 0; j < 3; j++) {
                        String msg = "test" + j;
                        String resp = client.sendMessage(msg);

                        Assert.assertEquals(msg, resp);

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
        server.start(5553);

        try {
            Client client = new Client();
            client.startConnection("127.0.0.1", 5553);

            server.stop();

            client.sendMessage("fail");

            Assert.fail();
        } catch (ConnectException ignored) {
        }
    }
}