package test.pack.client.v1;

import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.matcher.param.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.param.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import test.pack.client.v1.entity2.Model3;

public class ClientNumberTwo {
    private final Client client;

    private final ConvertParamStrategy convertParamStrategy = new DefaultConvertParamStrategy();

    public ClientNumberTwo(Client client) {
        this.client = client;
    }

    public Model3 method1(Model3 argument) throws ConnectException, ProtocolException,
            ConvertException {
        String name = "entity2.method1(ClientNumberTwo.entity2.model3)->ClientNumberTwo.entity2.model3";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(argument));
        Request req = new Request(name, arguments);

        Response resp = client.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "ClientNumberTwo.entity2.model3", Model3.class);
    }

    public List<Model3> method2(List<Model3> argument) throws ConnectException, ProtocolException,
            ConvertException {
        String name = "entity2.method2([ClientNumberTwo.entity2.model3])->[ClientNumberTwo.entity2.model3]";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(argument));
        Request req = new Request(name, arguments);

        Response resp = client.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "[ClientNumberTwo.entity2.model3]", List.class);
    }

    public Map<Model3, Model3> method3(Map<Model3, Model3> argument) throws ConnectException,
            ProtocolException, ConvertException {
        String name = "entity2.method3([ClientNumberTwo.entity2.model3]ClientNumberTwo.entity2.model3)->[ClientNumberTwo.entity2.model3]ClientNumberTwo.entity2.model3";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(argument));
        Request req = new Request(name, arguments);

        Response resp = client.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "[ClientNumberTwo.entity2.model3]ClientNumberTwo.entity2.model3", Map.class);
    }
}
