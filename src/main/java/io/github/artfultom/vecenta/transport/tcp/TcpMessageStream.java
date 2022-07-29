package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.transport.MessageHandler;
import io.github.artfultom.vecenta.transport.MessageStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class TcpMessageStream implements MessageStream {

    private final DataInputStream in;

    private final DataOutputStream out;

    private MessageHandler getHandler;

    private MessageHandler sendHandler;

    public TcpMessageStream(Socket socket) throws IOException {
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void setGetHandler(MessageHandler handler) {
        this.getHandler = handler;
    }

    @Override
    public void setSendHandler(MessageHandler handler) {
        this.sendHandler = handler;
    }

    @Override
    public byte[] getMessage() throws IOException {
        try {
            int size = in.readInt();
            byte[] bytes = in.readNBytes(size);

            if (getHandler != null) {
                bytes = getHandler.handle(bytes);
            }

            return bytes;
        } catch (EOFException e) {
            return new byte[0];
        }
    }

    @Override
    public void sendMessage(byte[] resp) throws IOException {
        if (sendHandler != null) {
            resp = sendHandler.handle(resp);
        }

        out.writeInt(resp.length);
        out.write(resp);
        out.flush();
    }
}
