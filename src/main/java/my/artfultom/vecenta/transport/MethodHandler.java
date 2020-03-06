package my.artfultom.vecenta.transport;

import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;

import java.util.function.Function;

public class MethodHandler {
    private final String name;
    private final Function<Request, Response> function;

    public MethodHandler(String name, Function<Request, Response> function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public Response execute(Request in) {
        return this.function.apply(in);
    }
}
