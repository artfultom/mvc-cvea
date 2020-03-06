package my.artfultom.vecenta.transport;

public interface ConvertStrategy {
    byte[] convertToBytes(Request in);

    byte[] convertToBytes(Response in);

    Response convertToResponse(byte[] in);

    Request convertToRequest(byte[] in);
}
