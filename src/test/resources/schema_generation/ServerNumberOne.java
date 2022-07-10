package test.pack.server.v1;

import io.github.artfultom.vecenta.matcher.annotations.RpcMethod;
import java.util.List;
import test.pack.server.v1.entity1.Model1;

public interface ServerNumberOne {
    @RpcMethod(
            entity = "entity1",
            name = "method1",
            argumentTypes = {"boolean", "[boolean]", "string", "[string]", "int8", "[int8]", "int16", "[int16]", "int32", "[int32]", "int64", "[int64]", "dec32", "[dec32]", "dec64", "[dec64]", "ClientNumberOne.entity1.model1", "[ClientNumberOne.entity1.model1]"},
            returnType = "boolean"
    )
    Boolean method1(Boolean field1, List<Boolean> field2, String field3, List<String> field4,
            Byte field5, List<Byte> field6, Short field7, List<Short> field8, Integer field9,
            List<Integer> field10, Long field11, List<Long> field12, Float field13,
            List<Float> field14, Double field15, List<Double> field16, Model1 field17,
            List<Model1> field18);
}
