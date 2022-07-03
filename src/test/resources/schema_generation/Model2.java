package test.pack.model.v1.entity1;

import io.github.artfultom.vecenta.matcher.Model;
import io.github.artfultom.vecenta.matcher.ModelField;

@Model(
        order = {"field1"}
)
public class Model2 {
    @ModelField(
            type = "model1"
    )
    public Model1 field1;

    public Model2() {
    }

    public Model1 getField1() {
        return this.field1;
    }

    public void setField1(Model1 field1) {
        this.field1 = field1;
    }
}
