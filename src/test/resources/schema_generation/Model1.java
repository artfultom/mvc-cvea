package test.pack.model.v1.entity1;

import io.github.artfultom.vecenta.matcher.Model;
import io.github.artfultom.vecenta.matcher.ModelField;
import java.util.List;

@Model(
        name = "model1",
        order = {"field1", "field2", "field3", "field4", "field5", "field6", "field7", "field8", "field9", "field10", "field11", "field12", "field13", "field14", "field15", "field16", "field17"}
)
public class Model1 {
    @ModelField(
            type = "boolean"
    )
    private Boolean field1;

    @ModelField(
            type = "[boolean]"
    )
    private List<Boolean> field2;

    @ModelField(
            type = "string"
    )
    private String field3;

    @ModelField(
            type = "[string]"
    )
    private List<String> field4;

    @ModelField(
            type = "int8"
    )
    private Byte field5;

    @ModelField(
            type = "[int8]"
    )
    private List<Byte> field6;

    @ModelField(
            type = "int16"
    )
    private Short field7;

    @ModelField(
            type = "[int16]"
    )
    private List<Short> field8;

    @ModelField(
            type = "int32"
    )
    private Integer field9;

    @ModelField(
            type = "[int32]"
    )
    private List<Integer> field10;

    @ModelField(
            type = "int64"
    )
    private Long field11;

    @ModelField(
            type = "[int64]"
    )
    private List<Long> field12;

    @ModelField(
            type = "dec32"
    )
    private Float field13;

    @ModelField(
            type = "[dec32]"
    )
    private List<Float> field14;

    @ModelField(
            type = "dec64"
    )
    private Double field15;

    @ModelField(
            type = "[dec64]"
    )
    private List<Double> field16;

    @ModelField(
            type = "[model2]"
    )
    private List<Model2> field17;

    public Model1() {
    }

    public Boolean getField1() {
        return this.field1;
    }

    public void setField1(Boolean field1) {
        this.field1 = field1;
    }

    public List<Boolean> getField2() {
        return this.field2;
    }

    public void setField2(List<Boolean> field2) {
        this.field2 = field2;
    }

    public String getField3() {
        return this.field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public List<String> getField4() {
        return this.field4;
    }

    public void setField4(List<String> field4) {
        this.field4 = field4;
    }

    public Byte getField5() {
        return this.field5;
    }

    public void setField5(Byte field5) {
        this.field5 = field5;
    }

    public List<Byte> getField6() {
        return this.field6;
    }

    public void setField6(List<Byte> field6) {
        this.field6 = field6;
    }

    public Short getField7() {
        return this.field7;
    }

    public void setField7(Short field7) {
        this.field7 = field7;
    }

    public List<Short> getField8() {
        return this.field8;
    }

    public void setField8(List<Short> field8) {
        this.field8 = field8;
    }

    public Integer getField9() {
        return this.field9;
    }

    public void setField9(Integer field9) {
        this.field9 = field9;
    }

    public List<Integer> getField10() {
        return this.field10;
    }

    public void setField10(List<Integer> field10) {
        this.field10 = field10;
    }

    public Long getField11() {
        return this.field11;
    }

    public void setField11(Long field11) {
        this.field11 = field11;
    }

    public List<Long> getField12() {
        return this.field12;
    }

    public void setField12(List<Long> field12) {
        this.field12 = field12;
    }

    public Float getField13() {
        return this.field13;
    }

    public void setField13(Float field13) {
        this.field13 = field13;
    }

    public List<Float> getField14() {
        return this.field14;
    }

    public void setField14(List<Float> field14) {
        this.field14 = field14;
    }

    public Double getField15() {
        return this.field15;
    }

    public void setField15(Double field15) {
        this.field15 = field15;
    }

    public List<Double> getField16() {
        return this.field16;
    }

    public void setField16(List<Double> field16) {
        this.field16 = field16;
    }

    public List<Model2> getField17() {
        return this.field17;
    }

    public void setField17(List<Model2> field17) {
        this.field17 = field17;
    }
}
