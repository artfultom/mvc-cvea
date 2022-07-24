package test.pack.exception.v1.entity1;

import io.github.artfultom.vecenta.matcher.annotations.RpcError;

@RpcError(
        name = "test"
)
public class TestException extends Exception {
}
