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
            argumentTypes = {"int32"},
            returnType = "int32"
    )
    Integer echo1(Integer a);

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
            argumentTypes = {"TestClient.math.Model3"},
            returnType = "TestClient.math.Model3"
    )
    Model3 echo3(Model3 a);

    @RpcMethod(
            entity = "math",
            name = "echo4",
            argumentTypes = {"[TestClient.math.Model3]"},
            returnType = "[TestClient.math.Model3]"
    )
    List<Model3> echo4(List<Model3> a);

    @RpcMethod(
            entity = "math",
            name = "echo5",
            argumentTypes = {"[int32]TestClient.math.Model3"},
            returnType = "[int32]TestClient.math.Model3"
    )
    Map<Integer, Model3> echo5(Map<Integer, Model3> a);

    @RpcMethod(
            entity = "math",
            name = "echo6",
            argumentTypes = {"[int32][TestClient.math.Model3]"},
            returnType = "[int32][TestClient.math.Model3]"
    )
    Map<Integer, List<Model3>> echo6(Map<Integer, List<Model3>> a);

    @RpcMethod(
            entity = "math",
            name = "echo7",
            argumentTypes = {"[[string]]"},
            returnType = "[[string]]"
    )
    List<List<String>> echo7(List<List<String>> a);

    @RpcMethod(
            entity = "math",
            name = "echo8",
            argumentTypes = {"[int32][[TestClient.math.Model3]]"},
            returnType = "[int32][[TestClient.math.Model3]]"
    )
    Map<Integer, List<List<Model3>>> echo8(Map<Integer, List<List<Model3>>> a);

    @RpcMethod(
            entity = "math",
            name = "echo9",
            argumentTypes = {"[[TestClient.math.Model3]][[TestClient.math.Model3]]"},
            returnType = "[[TestClient.math.Model3]][[TestClient.math.Model3]]"
    )
    Map<List<Model3>, List<List<Model3>>> echo9(Map<List<Model3>, List<List<Model3>>> a);

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
