package test.pack.server.v1;

import io.github.artfultom.vecenta.matcher.annotations.RpcMethod;
import java.util.List;
import java.util.Map;
import test.pack.server.v1.entity2.Model3;

public interface ServerNumberTwo {
    @RpcMethod(
            entity = "entity2",
            name = "method1",
            argumentTypes = {"ClientNumberTwo.entity2.model3"},
            returnType = "ClientNumberTwo.entity2.model3"
    )
    Model3 method1(Model3 argument);

    @RpcMethod(
            entity = "entity2",
            name = "method2",
            argumentTypes = {"[ClientNumberTwo.entity2.model3]"},
            returnType = "[ClientNumberTwo.entity2.model3]"
    )
    List<Model3> method2(List<Model3> argument);

    @RpcMethod(
            entity = "entity2",
            name = "method3",
            argumentTypes = {"[ClientNumberTwo.entity2.model3]ClientNumberTwo.entity2.model3"},
            returnType = "[ClientNumberTwo.entity2.model3]ClientNumberTwo.entity2.model3"
    )
    Map<Model3, Model3> method3(Map<Model3, Model3> argument);
}
