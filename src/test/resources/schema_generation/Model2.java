package test.pack.model.v1.entity1;

import io.github.artfultom.vecenta.matcher.Model;
import io.github.artfultom.vecenta.matcher.ModelField;

@Model(
        order = {"field1"}
)
public class Model2 {
    @ModelField(
            type = "[int32]"
    )
    public [int32] field1;

    public Model2() {
    }

    public [int32] getField1() {
        return this.field1;
    }

    public void setField1([int32] field1) {
        this.field1 = field1;
    }
}
