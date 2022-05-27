package io.github.artfultom.vecenta.generated.v1;

import io.github.artfultom.vecenta.matcher.Entity;

public interface SumServer {

    @Entity("math")
    java.lang.Integer sum(java.lang.Integer a, java.lang.Integer b);

    @Entity("math")
    java.lang.String concat(java.lang.String a, java.lang.String b, java.lang.String c);

}
