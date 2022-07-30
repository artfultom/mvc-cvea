package io.github.artfultom.vecenta.matcher;

import io.github.artfultom.vecenta.transport.error.ErrorType;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
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
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(out)
        ) {
            int methodLength = in.getMethodName().length();
            dataStream.writeInt(methodLength);
            dataStream.writeBytes(in.getMethodName());

            for (byte[] param : in.getParams()) {
                int paramLength = param.length;
                dataStream.writeInt(paramLength);
                dataStream.write(param);
            }

            return out.toByteArray();
        } catch (IOException e) {
            log.error("Cannot convert request to bytes.", e);
        }

        return new byte[0];
    }

    @Override
    public byte[] convertToBytes(Response in) {
        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(out)
        ) {
            if (in.getErrorType() == null) {
                dataStream.writeByte(0);

                byte[] param = in.getResult();
                dataStream.writeInt(param.length);
                dataStream.write(param);
            } else {
                dataStream.writeByte(1);
                dataStream.writeInt(in.getErrorType().ordinal());

                if (in.getErrorMsg() != null) {
                    dataStream.writeInt(in.getErrorMsg().length());
                    dataStream.write(TypeConverter.STRING.convert(in.getErrorMsg()));
                }
            }

            return out.toByteArray();
        } catch (IOException e) {
            log.error("Cannot convert response to bytes.", e);
        }

        return new byte[0];
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
            int size = buf.getInt();
            byte[] dst = new byte[size];
            buf.get(dst);

            return new Response(dst);
        } else {
            int errorCode = buf.getInt();
            ErrorType errorType = ErrorType.values()[errorCode];

            if (errorType == ErrorType.CHECKED_ERROR) {
                int size = buf.getInt();
                byte[] dst = new byte[size];
                buf.get(dst);

                String msg = (String) TypeConverter.STRING.convert(dst);
                return new Response(errorType, msg);
            }

            return new Response(errorType);
        }
    }
}
