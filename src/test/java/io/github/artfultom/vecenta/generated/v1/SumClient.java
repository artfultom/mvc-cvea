package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;

import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.List;

public class SumClient {
    private final Client client;

    public SumClient(Client client) {
        this.client = client;
    }

    public java.lang.Integer sum(java.lang.Integer a, java.lang.Integer b) throws ConnectException, ProtocolException {
        Request req = new Request(
                "math.sum(java.lang.Integer,java.lang.Integer)",
                List.of(ByteBuffer.allocate(4).putInt(a).array(), ByteBuffer.allocate(4).putInt(b).array())
        );

        Response resp = client.send(req);
        List<byte[]> result = resp.getResults();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return ByteBuffer.wrap(result.get(0)).getInt();
    }
}