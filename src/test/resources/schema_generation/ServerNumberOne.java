package test.pack.server.v1;

import io.github.artfultom.vecenta.matcher.RpcMethod;

public interface ServerNumberOne {
    @RpcMethod(
            entity = "entity_name",
            name = "method_name",
            argumentTypes = {"int32"},
            returnType = "boolean"
    )
    Boolean method_name(Integer argument_name);
}
