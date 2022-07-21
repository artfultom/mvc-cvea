package io.github.artfultom.vecenta.transport;

import java.io.IOException;

public interface MessageStream {

    byte[] getMessage() throws IOException;

    void sendMessage(byte[] resp) throws IOException;

}
