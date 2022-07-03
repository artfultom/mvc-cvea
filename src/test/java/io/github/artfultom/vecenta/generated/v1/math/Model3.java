package io.github.artfultom.vecenta.generated.v1.math;

import io.github.artfultom.vecenta.matcher.Model;
import io.github.artfultom.vecenta.matcher.ModelField;

@Model(
        order = {"field1", "field2", "field3", "field4"}
)
public class Model3 {
    @ModelField(
            type = "int32"
    )
    public Integer field1;

    @ModelField(
            type = "int16"
    )
    public Short field2;

    @ModelField(
            type = "string"
    )
    public String field3;

    @ModelField(
            type = "boolean"
    )
    public Boolean field4;

    public Model3() {
    }

    public Integer getField1() {
        return this.field1;
    }

    public void setField1(Integer field1) {
        this.field1 = field1;
    }

    public Short getField2() {
        return this.field2;
    }

    public void setField2(Short field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return this.field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public Boolean getField4() {
        return this.field4;
    }

    public void setField4(Boolean field4) {
        this.field4 = field4;
    }
}
