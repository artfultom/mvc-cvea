package io.github.artfultom.vecenta.generated.v1;

public class SumServerImpl implements SumServer {

    @Override
    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public String concat(String a, String b, String c) {
        return a + b + c;
    }
}
