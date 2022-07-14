package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.generated.v1.math.Model3;
import io.github.artfultom.vecenta.matcher.param.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.param.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Connector;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestClient {
    private final Connector connector;

    private final ConvertParamStrategy convertParamStrategy = new DefaultConvertParamStrategy();

    public TestClient(Connector connector) {
        this.connector = connector;
    }

    public Integer sum(Integer a, Integer b) throws ConnectException, ProtocolException,
            ConvertException {
        String name = "math.sum(int32,int32)->int32";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        arguments.add(convertParamStrategy.convertToByteArray(b));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "int32", Integer.class);
    }

    public String concat(String a, String b, String c) throws ConnectException, ProtocolException,
            ConvertException {
        String name = "math.concat(string,string,string)->string";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        arguments.add(convertParamStrategy.convertToByteArray(b));
        arguments.add(convertParamStrategy.convertToByteArray(c));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "string", String.class);
    }

    public Model3 echo(Model3 a) throws ConnectException, ProtocolException, ConvertException {
        String name = "math.echo(TestClient.math.Model3)->TestClient.math.Model3";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "TestClient.math.Model3", Model3.class);
    }

    public List<Integer> echo(List<Integer> a) throws ConnectException, ProtocolException,
            ConvertException {
        String name = "math.echo([int32])->[int32]";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "[int32]", List.class);
    }

    public List<Model3> echo(List<Model3> a, List<Model3> b) throws ConnectException,
            ProtocolException, ConvertException {
        String name = "math.echo([TestClient.math.Model3],[TestClient.math.Model3])->[TestClient.math.Model3]";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        arguments.add(convertParamStrategy.convertToByteArray(b));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "[TestClient.math.Model3]", List.class);
    }

    public Map<Integer, Model3> echo(Map<Integer, Model3> a) throws ConnectException,
            ProtocolException, ConvertException {
        String name = "math.echo([int32]TestClient.math.Model3)->[int32]TestClient.math.Model3";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "[int32]TestClient.math.Model3", Map.class);
    }

    public Map<Integer, List<Model3>> echo(Map<Integer, List<Model3>> a,
            Map<Integer, List<Model3>> b) throws ConnectException, ProtocolException,
            ConvertException {
        String name = "math.echo([int32][TestClient.math.Model3],[int32][TestClient.math.Model3])->[int32][TestClient.math.Model3]";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        arguments.add(convertParamStrategy.convertToByteArray(b));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "[int32][TestClient.math.Model3]", Map.class);
    }
}
