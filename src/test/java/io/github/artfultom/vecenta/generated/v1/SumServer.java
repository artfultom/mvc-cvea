package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.matcher.RpcMethod;

public interface SumServer {
    @RpcMethod(
            entity = "math",
            name = "math.sum(int32,int32)"
    )
    Integer sum(Integer a, Integer b);

    @RpcMethod(
            entity = "math",
            name = "math.concat(string,string,string)"
    )
    String concat(String a, String b, String c);
}
