package io.github.artfultom.vecenta.generation;

import java.util.List;

public interface ClassGenerator {

    Builder prepare(String fileName, Data data);

    interface Builder {

        Builder server();

        Builder client();

        List<GeneratedCode> result();
    }
}
