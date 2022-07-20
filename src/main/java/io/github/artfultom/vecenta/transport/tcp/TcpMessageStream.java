package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.transport.MessageStream;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TcpMessageStream implements MessageStream {

    DataInputStream in;

    DataOutputStream out;

    public TcpMessageStream(Socket socket) {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getMessage() throws ConnectionException {
        try {
            int size = in.readInt();
            return in.readNBytes(size);
        } catch (IOException e) {
            throw new ConnectionException("Cannot get the message.", e);
        }
    }

    @Override
    public void sendMessage(byte[] resp) throws ConnectionException {
        try {
            out.writeInt(resp.length);
            out.write(resp);
            out.flush();
        } catch (IOException e) {
            throw new ConnectionException("Cannot send the message.", e);
        }
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
    }
}
