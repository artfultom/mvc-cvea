package my.artfultom.vecenta.controller;

import my.artfultom.vecenta.matcher.ServerMatcher;
import my.artfultom.vecenta.transport.*;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;
import my.artfultom.vecenta.transport.tcp.TcpClient;
import my.artfultom.vecenta.transport.tcp.TcpServer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.List;

public class ControllerTest {

    @Test
    public void test() throws ConnectException {
        Server server = new TcpServer();
        server.start(5550);
        ServerMatcher serverMatcher = new ServerMatcher(server);
        serverMatcher.register(ServerController.class); // TODO one or many?

        Client client = new TcpClient();
        client.startConnection("127.0.0.1", 5550);
        ClientConnector clientConnector = new ClientConnector(client);
        int result = clientConnector.sum(3, 4);

        Assert.assertEquals(5, result);

        try {
            client.stopConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        server.stop();
    }

    public static class ClientConnector {
        private Client client;

        public ClientConnector(Client client) {
            this.client = client;
        }

        // TODO must be generated
        public Integer sum(Integer a, int b) {
            Request req = new Request(
                    "ServerController.sum(java.lang.Integer,int)",
                    List.of(ByteBuffer.allocate(4).putInt(2).array(), ByteBuffer.allocate(4).putInt(3).array())
            );
            Response resp = null;

            try {
                resp = client.send(req);
            } catch (ConnectException e) {
                e.printStackTrace();
                return null;
            }

            return ByteBuffer.wrap(resp.getParams().get(0)).getInt();
        }
    }

    public static class ServerController {

        public Integer sum(Integer a, int b) {
            return a + b;
        }
    }
}