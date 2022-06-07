package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.matcher.Entity;

public interface SumServer {
    @Entity("math")
    Integer sum(Integer a, Integer b);

    @Entity("math")
    String concat(String a, String b, String c);
}
