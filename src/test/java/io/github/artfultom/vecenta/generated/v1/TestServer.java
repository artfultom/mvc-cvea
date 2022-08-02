package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.generated.v1.math.EtcException;
import io.github.artfultom.vecenta.generated.v1.math.FileNotFoundException;
import io.github.artfultom.vecenta.generated.v1.math.IOException;
import io.github.artfultom.vecenta.generated.v1.math.Model3;
import io.github.artfultom.vecenta.generated.v1.math.NewErrorException;
import io.github.artfultom.vecenta.matcher.annotations.RpcMethod;
import java.util.List;
import java.util.Map;

public interface TestServer {
    @RpcMethod(
            entity = "math",
            name = "sum",
            argumentTypes = {"int32", "int32"},
            returnType = "int32"
    )
    Integer sum(Integer a, Integer b);

    @RpcMethod(
            entity = "math",
            name = "concat",
            argumentTypes = {"string", "string", "string"},
            returnType = "string"
    )
    String concat(String a, String b, String c);

    @RpcMethod(
            entity = "math",
            name = "echo1",
            argumentTypes = {"TestClient.math.Model3"},
            returnType = "TestClient.math.Model3"
    )
    Model3 echo1(Model3 a);

    @RpcMethod(
            entity = "math",
            name = "echo2",
            argumentTypes = {"[int32]"},
            returnType = "[int32]"
    )
    List<Integer> echo2(List<Integer> a);

    @RpcMethod(
            entity = "math",
            name = "echo3",
            argumentTypes = {"[TestClient.math.Model3]", "[TestClient.math.Model3]"},
            returnType = "[TestClient.math.Model3]"
    )
    List<Model3> echo3(List<Model3> a, List<Model3> b);

    @RpcMethod(
            entity = "math",
            name = "echo4",
            argumentTypes = {"[int32]TestClient.math.Model3"},
            returnType = "[int32]TestClient.math.Model3"
    )
    Map<Integer, Model3> echo4(Map<Integer, Model3> a);

    @RpcMethod(
            entity = "math",
            name = "echo5",
            argumentTypes = {"[int32][TestClient.math.Model3]", "[int32][TestClient.math.Model3]"},
            returnType = "[int32][TestClient.math.Model3]"
    )
    Map<Integer, List<Model3>> echo5(Map<Integer, List<Model3>> a, Map<Integer, List<Model3>> b);

    @RpcMethod(
            entity = "math",
            name = "echo6",
            argumentTypes = {"[[string]]"},
            returnType = "[[string]]"
    )
    List<List<String>> echo6(List<List<String>> a);

    @RpcMethod(
            entity = "math",
            name = "echo7",
            argumentTypes = {"[int32][[TestClient.math.Model3]]", "[int32][[TestClient.math.Model3]]"},
            returnType = "[int32][[TestClient.math.Model3]]"
    )
    Map<Integer, List<List<Model3>>> echo7(Map<Integer, List<List<Model3>>> a,
            Map<Integer, List<List<Model3>>> b);

    @RpcMethod(
            entity = "math",
            name = "supply",
            returnType = "int32"
    )
    Integer supply();

    @RpcMethod(
            entity = "math",
            name = "consume",
            argumentTypes = {"int32"}
    )
    void consume(Integer a);

    @RpcMethod(
            entity = "math",
            name = "error1"
    )
    void error1();

    @RpcMethod(
            entity = "math",
            name = "error2",
            errors = {"file not found", "new error"}
    )
    void error2() throws FileNotFoundException, NewErrorException;

    @RpcMethod(
            entity = "math",
            name = "error3",
            errors = {"file not found", "i/o", "etc"}
    )
    void error3() throws FileNotFoundException, IOException, EtcException;
}
