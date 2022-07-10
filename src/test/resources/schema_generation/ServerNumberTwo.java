package test.pack.server.v1;

import io.github.artfultom.vecenta.matcher.annotations.RpcMethod;

public interface ServerNumberTwo {
    @RpcMethod(
            entity = "entity2",
            name = "method_name",
            argumentTypes = {"int32"},
            returnType = "int32"
    )
    Integer method_name(Integer argument_name);
}
