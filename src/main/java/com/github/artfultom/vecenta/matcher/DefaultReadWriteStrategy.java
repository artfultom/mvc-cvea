package com.github.artfultom.vecenta.matcher;

import com.github.artfultom.vecenta.transport.error.MessageError;
import com.github.artfultom.vecenta.transport.message.Request;
import com.github.artfultom.vecenta.transport.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultReadWriteStrategy implements ReadWriteStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultReadWriteStrategy.class);

    @Override
    public byte[] convertToBytes(Request in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(out);

        try {
            int methodLength = in.getMethodName().length();
            dataStream.writeInt(methodLength);
            dataStream.writeBytes(in.getMethodName());

            for (byte[] param : in.getParams()) {
                int paramLength = param.length;
                dataStream.writeInt(paramLength);
                dataStream.write(param);
            }
        } catch (IOException e) {
            log.error("cannot convert request to bytes", e);
        }

        return out.toByteArray();
    }

    @Override
    public byte[] convertToBytes(Response in) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(out);

        try {
            if (in.getError() == null) {
                dataStream.writeByte(0);
                for (byte[] param : in.getResults()) {
                    dataStream.writeInt(param.length);
                    dataStream.write(param);
                }
            } else {
                dataStream.writeByte(1);
                dataStream.writeInt(in.getError().ordinal());
            }
        } catch (IOException e) {
            log.error("cannot convert response to bytes", e);
        }

        return out.toByteArray();
    }

    @Override
    public Request convertToRequest(byte[] in) {
        ByteBuffer buf = ByteBuffer.wrap(in);

        int methodSize = buf.getInt(0);

        byte[] rawMethod = Arrays.copyOfRange(in, Integer.BYTES, methodSize + Integer.BYTES);
        String method = new String(rawMethod, StandardCharsets.UTF_8);

        List<byte[]> params = new ArrayList<>();
        for (int i = methodSize + Integer.BYTES; i < buf.capacity(); ) {
            byte[] rawSize = Arrays.copyOfRange(in, i, i + Integer.BYTES);
            int paramSize = ByteBuffer.wrap(rawSize).getInt();

            byte[] param = Arrays.copyOfRange(in, i + Integer.BYTES, paramSize + i + Integer.BYTES);

            params.add(param);

            i += paramSize + Integer.BYTES;
        }

        return new Request(method, params);
    }

    @Override
    public Response convertToResponse(byte[] in) {
        ByteBuffer buf = ByteBuffer.wrap(in);

        byte errorFlag = buf.get();

        if (errorFlag == 0) {
            List<byte[]> params = new ArrayList<>();

            for (int i = 1; i < buf.capacity(); ) {
                byte[] rawSize = Arrays.copyOfRange(in, i, i + Integer.BYTES);
                int size = ByteBuffer.wrap(rawSize).getInt();
                byte[] param = Arrays.copyOfRange(in, i + Integer.BYTES, i + Integer.BYTES + size);

                params.add(param);

                i += size + Integer.BYTES;
            }

            return new Response(params);
        } else {
            int errorCode = buf.getInt();

            return new Response(MessageError.values()[errorCode]);
        }
    }
}
