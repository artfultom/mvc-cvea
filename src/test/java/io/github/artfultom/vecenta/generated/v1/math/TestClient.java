package io.github.artfultom.vecenta.generated.v1.math;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.matcher.param.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.param.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.Connector;
import io.github.artfultom.vecenta.transport.error.ErrorType;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestClient {
    private final Connector connector;

    private final ConvertParamStrategy convertParamStrategy = new DefaultConvertParamStrategy();

    public TestClient(Connector connector) {
        this.connector = connector;
    }

    public Integer sum(Integer a, Integer b) throws ConnectionException, ConvertException,
            ProtocolException {
        String name = "math.sum(int32,int32)->int32";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        arguments.add(convertParamStrategy.convertToByteArray(b));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }

        byte[] result = resp.getResult();
        return convertParamStrategy.convertToObject(result, "int32", Integer.class);
    }

    public String concat(String a, String b, String c) throws ConnectionException, ConvertException,
            ProtocolException {
        String name = "math.concat(string,string,string)->string";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        arguments.add(convertParamStrategy.convertToByteArray(b));
        arguments.add(convertParamStrategy.convertToByteArray(c));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }

        byte[] result = resp.getResult();
        return convertParamStrategy.convertToObject(result, "string", String.class);
    }

    public Model3 echo(Model3 a) throws ConnectionException, ConvertException, ProtocolException {
        String name = "math.echo(TestClient.math.Model3)->TestClient.math.Model3";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }

        byte[] result = resp.getResult();
        return convertParamStrategy.convertToObject(result, "TestClient.math.Model3", Model3.class);
    }

    public List<Integer> echo(List<Integer> a) throws ConnectionException, ConvertException,
            ProtocolException {
        String name = "math.echo([int32])->[int32]";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }

        byte[] result = resp.getResult();
        return convertParamStrategy.convertToObject(result, "[int32]", List.class);
    }

    public List<Model3> echo(List<Model3> a, List<Model3> b) throws ConnectionException,
            ConvertException, ProtocolException {
        String name = "math.echo([TestClient.math.Model3],[TestClient.math.Model3])->[TestClient.math.Model3]";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        arguments.add(convertParamStrategy.convertToByteArray(b));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }

        byte[] result = resp.getResult();
        return convertParamStrategy.convertToObject(result, "[TestClient.math.Model3]", List.class);
    }

    public Map<Integer, Model3> echo(Map<Integer, Model3> a) throws ConnectionException,
            ConvertException, ProtocolException {
        String name = "math.echo([int32]TestClient.math.Model3)->[int32]TestClient.math.Model3";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }

        byte[] result = resp.getResult();
        return convertParamStrategy.convertToObject(result, "[int32]TestClient.math.Model3", Map.class);
    }

    public Map<Integer, List<Model3>> echo(Map<Integer, List<Model3>> a,
            Map<Integer, List<Model3>> b) throws ConnectionException, ConvertException,
            ProtocolException {
        String name = "math.echo([int32][TestClient.math.Model3],[int32][TestClient.math.Model3])->[int32][TestClient.math.Model3]";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        arguments.add(convertParamStrategy.convertToByteArray(b));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }

        byte[] result = resp.getResult();
        return convertParamStrategy.convertToObject(result, "[int32][TestClient.math.Model3]", Map.class);
    }

    public Integer supply() throws ConnectionException, ConvertException, ProtocolException {
        String name = "math.supply()->int32";
        List<byte[]> arguments = new ArrayList<>();
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }

        byte[] result = resp.getResult();
        return convertParamStrategy.convertToObject(result, "int32", Integer.class);
    }

    public void consume(Integer a) throws ConnectionException, ConvertException, ProtocolException {
        String name = "math.consume(int32)";
        List<byte[]> arguments = new ArrayList<>();
        arguments.add(convertParamStrategy.convertToByteArray(a));
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }
    }

    public void error1() throws ConnectionException, ProtocolException {
        String name = "math.error1()";
        List<byte[]> arguments = new ArrayList<>();
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }
    }

    public void error2() throws ConnectionException, ProtocolException, FileNotFoundException,
            NewErrorException {
        String name = "math.error2()";
        List<byte[]> arguments = new ArrayList<>();
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    case "file not found": throw new FileNotFoundException();
                    case "new error": throw new NewErrorException();
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }
    }

    public void error3() throws ConnectionException, ProtocolException, FileNotFoundException,
            IOException, EtcException {
        String name = "math.error3()";
        List<byte[]> arguments = new ArrayList<>();
        Request req = new Request(name, arguments);

        Response resp = connector.send(req);
        if (resp.getErrorType() != null) {
            if (resp.getErrorType() == ErrorType.CHECKED_ERROR) {
                switch(resp.getErrorMsg()) {
                    case "file not found": throw new FileNotFoundException();
                    case "i/o": throw new IOException();
                    case "etc": throw new EtcException();
                    default: throw new RuntimeException();
                }
            }
            if (resp.getErrorType() == ErrorType.UNKNOWN_METHOD_ERROR) {
                throw new RuntimeException();
            }
            throw new ProtocolException(resp.getErrorType());
        }
    }
}
