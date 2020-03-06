package my.artfultom.vecenta.transport;

import java.io.IOException;
import java.net.ConnectException;

public interface Client {

    void startConnection(String host, int port) throws ConnectException;

    Response send(Request request) throws ConnectException;

    void stopConnection() throws IOException;
}
