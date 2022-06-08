package test.pack.server.v1;

import io.github.artfultom.vecenta.matcher.RpcMethod;

public interface ServerNumberOne {
    @RpcMethod(
            entity = "entity_name",
            name = "entity_name.method_name(int32)"
    )
    Boolean method_name(Integer argument_name);
}
