package test.pack.server.v1;

import io.github.artfultom.vecenta.matcher.Entity;

public interface ServerNumberOne {
    @Entity("entity_name")
    Boolean method_name(Integer argument_name);
}
