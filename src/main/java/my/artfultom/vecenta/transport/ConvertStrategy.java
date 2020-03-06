package my.artfultom.vecenta.transport;

import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;

public interface ConvertStrategy {

    byte[] convertToBytes(Request in);

    byte[] convertToBytes(Response in);

    Response convertToResponse(byte[] in);

    Request convertToRequest(byte[] in);
}
