package io.github.artfultom.vecenta.transport;

import java.io.IOException;

public interface MessageStream {

    void setGetHandler(MessageHandler handler);

    void setSendHandler(MessageHandler handler);

    byte[] getMessage() throws IOException;

    void sendMessage(byte[] resp) throws IOException;

}
