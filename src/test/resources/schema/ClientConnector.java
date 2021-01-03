package my.artfultom.vecenta.client.v1;

import my.artfultom.vecenta.transport.Client;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;

import java.net.ConnectException;
import java.nio.ByteBuffer;

public class ClientConnector {
    private final Client client;

    public ClientConnector(Client client) {
        this.client = client;
    }

    public Integer method_name(my.artfultom.vecenta.controller.v1.A argument_name) {
        Request req = new Request(null, null);
        try {
            Response resp = client.send(req);
            return ByteBuffer.wrap(resp.getResults().get(0)).getInt();
        } catch (ConnectException e) {
            e.printStackTrace();
            return null;
        }
    }
}