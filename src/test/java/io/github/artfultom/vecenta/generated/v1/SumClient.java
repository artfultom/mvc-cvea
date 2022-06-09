package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.impl.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class SumClient {
    private final Client client;

    private final ConvertParamStrategy convertParamStrategy = new DefaultConvertParamStrategy();

    public SumClient(Client client) {
        this.client = client;
    }

    public Integer sum(Integer a, Integer b) throws ConnectException, ProtocolException {
        String name = "math.sum(int32,int32)";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(Integer.class, a));
        arguments.add(convertParamStrategy.convertToByteArray(Integer.class, b));
        Request req = new Request(name, arguments);

        Response resp = client.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(Integer.class, result);
    }

    public String concat(String a, String b, String c) throws ConnectException, ProtocolException {
        String name = "math.concat(string,string,string)";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(String.class, a));
        arguments.add(convertParamStrategy.convertToByteArray(String.class, b));
        arguments.add(convertParamStrategy.convertToByteArray(String.class, c));
        Request req = new Request(name, arguments);

        Response resp = client.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(String.class, result);
    }
}
