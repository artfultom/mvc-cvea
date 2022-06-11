package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.generated.Model1;
import io.github.artfultom.vecenta.matcher.RpcMethod;

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
            argumentTypes = {"Model1"},
            returnType = "Model1"
    )
    Model1 echo(Model1 a);
}
