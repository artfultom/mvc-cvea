package io.github.artfultom.vecenta.matcher;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public enum Converter {

    BOOLEAN(Boolean.class) {
        @Override
        public byte[] convert(Object in) {
            byte[] result = new byte[1];
            result[0] = (byte) ((boolean) in ? 1 : 0);
            return result;
        }

        @Override
        public Boolean convert(byte[] in) {
            return ByteBuffer.wrap(in).get() == 1;
        }
    },
    BYTE(Byte.class) {
        @Override
        public byte[] convert(Object in) {
            return ByteBuffer.allocate(Byte.BYTES).put((Byte) in).array();
        }

        @Override
        public Byte convert(byte[] in) {
            return ByteBuffer.wrap(in).get();
        }
    },
    SHORT(Short.class) {
        @Override
        public byte[] convert(Object in) {
            return ByteBuffer.allocate(Short.BYTES).putShort((Short) in).array();
        }

        @Override
        public Short convert(byte[] in) {
            return ByteBuffer.wrap(in).getShort();
        }
    },
    INTEGER(Integer.class) {
        @Override
        public byte[] convert(Object in) {
            return ByteBuffer.allocate(Integer.BYTES).putInt((Integer) in).array();
        }

        @Override
        public Integer convert(byte[] in) {
            return ByteBuffer.wrap(in).getInt();
        }
    },
    LONG(Long.class) {
        @Override
        public byte[] convert(Object in) {
            return ByteBuffer.allocate(Long.BYTES).putLong((Long) in).array();
        }

        @Override
        public Long convert(byte[] in) {
            return ByteBuffer.wrap(in).getLong();
        }
    },
    FLOAT(Float.class) {
        @Override
        public byte[] convert(Object in) {
            return ByteBuffer.allocate(Float.BYTES).putFloat((Float) in).array();
        }

        @Override
        public Float convert(byte[] in) {
            return ByteBuffer.wrap(in).getFloat();
        }
    },
    DOUBLE(Double.class) {
        @Override
        public byte[] convert(Object in) {
            return ByteBuffer.allocate(Double.BYTES).putDouble((Double) in).array();
        }

        @Override
        public Double convert(byte[] in) {
            return ByteBuffer.wrap(in).getDouble();
        }
    },
    STRING(String.class) {
        @Override
        public byte[] convert(Object in) {
            return ((String) in).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String convert(byte[] in) {
            return new String(in, StandardCharsets.UTF_8);
        }
    };

    private final Class<?> clazz;

    Converter(Class<?> clazz) {
        this.clazz = clazz;
    }

    public abstract byte[] convert(Object in);

    public abstract Object convert(byte[] in);

    public static Converter get(Class<?> clazz) {
        for (Converter val : Converter.values()) {
            if (val.clazz.equals(clazz)) {
                return val;
            }
        }

        return null;
    }
}
