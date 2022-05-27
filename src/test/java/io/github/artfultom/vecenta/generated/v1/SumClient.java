package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;

import java.net.ConnectException;
import java.util.List;

public class SumClient {

    private final Client client;

    private final ConvertParamStrategy convertParamStrategy = new DefaultConvertParamStrategy();

    public SumClient(Client client) {
        this.client = client;
    }

    public java.lang.Integer sum(java.lang.Integer a, java.lang.Integer b) throws ConnectException, ProtocolException {
        Request req = new Request(
                "math.sum(java.lang.Integer,java.lang.Integer)",
                List.of(convertParamStrategy.convertToByteArray(java.lang.Integer.class, a),convertParamStrategy.convertToByteArray(java.lang.Integer.class, b))
        );

        Response resp = client.send(req);
        List<byte[]> result = resp.getResults();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(java.lang.Integer.class, result.get(0));
    }
    public java.lang.String concat(java.lang.String a, java.lang.String b, java.lang.String c) throws ConnectException, ProtocolException {
        Request req = new Request(
                "math.concat(java.lang.String,java.lang.String,java.lang.String)",
                List.of(convertParamStrategy.convertToByteArray(java.lang.String.class, a),convertParamStrategy.convertToByteArray(java.lang.String.class, b),convertParamStrategy.convertToByteArray(java.lang.String.class, c))
        );

        Response resp = client.send(req);
        List<byte[]> result = resp.getResults();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(java.lang.String.class, result.get(0));
    }
}