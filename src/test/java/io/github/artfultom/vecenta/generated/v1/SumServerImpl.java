package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.generated.Model1;

public class SumServerImpl implements SumServer {

    @Override
    public Integer sum(Integer a, Integer b) {
        return a + b;
    }

    @Override
    public String concat(String a, String b, String c) {
        return a + b + c;
    }

    @Override
    public Model1 echo(Model1 a) {
        return a;
    }
}
