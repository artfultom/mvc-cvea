package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.generated.v1.math.Model3;
import io.github.artfultom.vecenta.matcher.RpcMethod;
import java.util.List;
import java.util.Map;

public interface SumServer {
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
            name = "echo",
            argumentTypes = {"Model3"},
            returnType = "Model3"
    )
    Model3 echo(Model3 a);

    @RpcMethod(
            entity = "math",
            name = "echo",
            argumentTypes = {"[int32]"},
            returnType = "[int32]"
    )
    List<Integer> echo(List<Integer> a);

    @RpcMethod(
            entity = "math",
            name = "echo",
            argumentTypes = {"[Model3]", "[Model3]"},
            returnType = "[Model3]"
    )
    List<Model3> echo(List<Model3> a, List<Model3> b);

    @RpcMethod(
            entity = "math",
            name = "echo",
            argumentTypes = {"[int32]Model3"},
            returnType = "[int32]Model3"
    )
    Map<Integer, Model3> echo(Map<Integer, Model3> a);

    @RpcMethod(
            entity = "math",
            name = "echo",
            argumentTypes = {"[int32][Model3]", "[int32][Model3]"},
            returnType = "[int32][Model3]"
    )
    Map<Integer, List<Model3>> echo(Map<Integer, List<Model3>> a, Map<Integer, List<Model3>> b);
}
