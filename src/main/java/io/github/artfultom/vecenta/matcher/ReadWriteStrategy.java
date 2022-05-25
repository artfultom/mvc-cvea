package io.github.artfultom.vecenta.matcher;

import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;

public interface ReadWriteStrategy {

    byte[] convertToBytes(Request in);

    byte[] convertToBytes(Response in);

    Request convertToRequest(byte[] in);

    Response convertToResponse(byte[] in);
}
