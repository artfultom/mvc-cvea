package my.artfultom.vecenta.controller;

import my.artfultom.vecenta.matcher.ControllerMatcher;
import my.artfultom.vecenta.transport.Client;
import my.artfultom.vecenta.transport.Request;
import my.artfultom.vecenta.transport.Response;
import my.artfultom.vecenta.transport.Server;
import my.artfultom.vecenta.util.RpcMethod;
import org.junit.Assert;
import org.junit.Test;

import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.List;

public class ControllerTest {

    @Test
    public void test() throws ConnectException {
        Server server = new Server();
        server.start(5550);
        ControllerMatcher matcher = new ControllerMatcher(server);
        matcher.registerController(ServerController.class);

        Client client = new Client();
        client.startConnection("127.0.0.1", 5550);
        Request req = new Request(
                "ServerController.sum(java.lang.Integer,int)",
                List.of(ByteBuffer.allocate(4).putInt(2).array(), ByteBuffer.allocate(4).putInt(3).array())
        );
        Response resp = client.send(req);

        Assert.assertEquals(5, ByteBuffer.wrap(resp.getParams().get(0)).getInt());
    }

    public static class ServerController {

        @RpcMethod
        public Integer sum(Integer a, int b) {
            return a + b;
        }
    }
}