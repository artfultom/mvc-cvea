package my.artfultom.vecenta.generated.v1;

import my.artfultom.vecenta.transport.Client;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;

import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.List;

public class SumClient {
    private final Client client;

    public SumClient(Client client) {
        this.client = client;
    }

    public Integer sum(java.lang.Integer a, java.lang.Integer b) {
        Request req = new Request(
                "math.sum(java.lang.Integer,java.lang.Integer)",
                List.of(ByteBuffer.allocate(4).putInt(a).array(), ByteBuffer.allocate(4).putInt(b).array())
        );
        try {
            Response resp = client.send(req);
            return ByteBuffer.wrap(resp.getResults().get(0)).getInt();
        } catch (ConnectException e) {
            e.printStackTrace();
            return null;
        }
    }
}