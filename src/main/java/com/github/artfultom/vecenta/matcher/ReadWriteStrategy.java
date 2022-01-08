package com.github.artfultom.vecenta.matcher;

import com.github.artfultom.vecenta.transport.message.Request;
import com.github.artfultom.vecenta.transport.message.Response;

public interface ReadWriteStrategy {

    byte[] convertToBytes(Request in);

    byte[] convertToBytes(Response in);

    Request convertToRequest(byte[] in);

    Response convertToResponse(byte[] in);
}
