package io.github.artfultom.vecenta.transport.tcp;

import io.github.artfultom.vecenta.transport.MessageStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

public class TcpMessageStream implements MessageStream {

    DataInputStream in;

    DataOutputStream out;

    public TcpMessageStream(Socket socket) throws IOException {
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public byte[] getMessage() throws IOException {
        try {
            int size = in.readInt();
            return in.readNBytes(size);
        } catch (EOFException e) {
            return new byte[0];
        }
    }

    @Override
    public void sendMessage(byte[] resp) throws IOException {
        out.writeInt(resp.length);
        out.write(resp);
        out.flush();
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
