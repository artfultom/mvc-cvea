package test.pack.model.v1.entity2;

import io.github.artfultom.vecenta.matcher.annotations.Model;
import io.github.artfultom.vecenta.matcher.annotations.ModelField;
import java.util.List;
import java.util.Map;

@Model(
        name = "ClientNumberTwo.entity2.model3",
        order = {"field1", "field1", "field1"}
)
public class Model3 {
    @ModelField(
            type = "[boolean]"
    )
    private List<Boolean> field1;

    @ModelField(
            type = "[boolean]boolean"
    )
    private Map<Boolean, Boolean> field1;

    @ModelField(
            type = "[boolean][boolean]"
    )
    private Map<Boolean, List<Boolean>> field1;

    public Model3() {
    }

    public List<Boolean> getField1() {
        return this.field1;
    }

    public void setField1(List<Boolean> field1) {
        this.field1 = field1;
    }

    public Map<Boolean, Boolean> getField1() {
        return this.field1;
    }

    public void setField1(Map<Boolean, Boolean> field1) {
        this.field1 = field1;
    }

    public Map<Boolean, List<Boolean>> getField1() {
        return this.field1;
    }

    public void setField1(Map<Boolean, List<Boolean>> field1) {
        this.field1 = field1;
    }
}
