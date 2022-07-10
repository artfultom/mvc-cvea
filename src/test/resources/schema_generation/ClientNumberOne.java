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
import test.pack.client.v1.entity1.Model1;

public class ClientNumberOne {
    private final Client client;

    private final ConvertParamStrategy convertParamStrategy = new DefaultConvertParamStrategy();

    public ClientNumberOne(Client client) {
        this.client = client;
    }

    public Boolean method1(Boolean field1, List<Boolean> field2, String field3, List<String> field4,
            Byte field5, List<Byte> field6, Short field7, List<Short> field8, Integer field9,
            List<Integer> field10, Long field11, List<Long> field12, Float field13,
            List<Float> field14, Double field15, List<Double> field16, Model1 field17,
            List<Model1> field18) throws ConnectException, ProtocolException, ConvertException {
        String name = "entity1.method1(boolean,[boolean],string,[string],int8,[int8],int16,[int16],int32,[int32],int64,[int64],dec32,[dec32],dec64,[dec64],ClientNumberOne.entity1.model1,[ClientNumberOne.entity1.model1])->boolean";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(field1));
        arguments.add(convertParamStrategy.convertToByteArray(field2));
        arguments.add(convertParamStrategy.convertToByteArray(field3));
        arguments.add(convertParamStrategy.convertToByteArray(field4));
        arguments.add(convertParamStrategy.convertToByteArray(field5));
        arguments.add(convertParamStrategy.convertToByteArray(field6));
        arguments.add(convertParamStrategy.convertToByteArray(field7));
        arguments.add(convertParamStrategy.convertToByteArray(field8));
        arguments.add(convertParamStrategy.convertToByteArray(field9));
        arguments.add(convertParamStrategy.convertToByteArray(field10));
        arguments.add(convertParamStrategy.convertToByteArray(field11));
        arguments.add(convertParamStrategy.convertToByteArray(field12));
        arguments.add(convertParamStrategy.convertToByteArray(field13));
        arguments.add(convertParamStrategy.convertToByteArray(field14));
        arguments.add(convertParamStrategy.convertToByteArray(field15));
        arguments.add(convertParamStrategy.convertToByteArray(field16));
        arguments.add(convertParamStrategy.convertToByteArray(field17));
        arguments.add(convertParamStrategy.convertToByteArray(field18));
        Request req = new Request(name, arguments);

        Response resp = client.send(req);
        byte[] result = resp.getResult();
        if (result == null) {
            throw new ProtocolException(resp.getError());
        }

        return convertParamStrategy.convertToObject(result, "boolean", Boolean.class);
    }
}
