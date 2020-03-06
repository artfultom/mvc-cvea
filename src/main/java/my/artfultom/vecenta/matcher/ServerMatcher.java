package my.artfultom.vecenta.matcher;

import my.artfultom.vecenta.transport.MethodHandler;
import my.artfultom.vecenta.transport.Server;
import my.artfultom.vecenta.transport.message.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ServerMatcher {

    private final Server server;

    public ServerMatcher(Server server) {
        this.server = server;
    }

    public boolean register(Class<?> controllerClass) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            MethodHandler handler = new MethodHandler(getName(method), (request) -> {
                try {
                    Class<?> returnType = method.getReturnType();

                    List<Object> requestParams = new ArrayList<>();
                    for (int i = 0; i < request.getParams().size(); i++) {
                        byte[] param = request.getParams().get(i);

                        requestParams.add(convertToObject(method.getParameterTypes()[i], param));
                    }

                    Object result = method.invoke(
                            controllerClass.getDeclaredConstructor().newInstance(),
                            requestParams.toArray()
                    );

                    List<byte[]> responseParams = List.of(convertToByteArray(returnType, result));

                    return new Response(responseParams);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                return new Response(1);
            });

            server.register(handler);
        }

        return true;
    }

    private byte[] convertToByteArray(Class<?> clazz, Object in) {
        byte[] result = null;

        switch (clazz.getName()) {
            case "char":
                break;
            case "short":
                break;
            case "int":
            case "java.lang.Integer":
                result = ByteBuffer.allocate(Integer.BYTES).putInt((Integer) in).array();
                break;
            case "long":
                break;
            case "float":
                break;
            case "double":
                break;
            default:
                System.err.println("Unknown type: " + clazz.getName());
        }

        return result;
    }

    private Object convertToObject(Class<?> clazz, byte[] in) {
        Object result = null;

        switch (clazz.getName()) {
            case "char":
                result = ByteBuffer.wrap(in).getChar();
                break;
            case "short":
                result = ByteBuffer.wrap(in).getShort();
                break;
            case "int":
            case "java.lang.Integer":
                result = ByteBuffer.wrap(in).getInt();
                break;
            case "long":
                result = ByteBuffer.wrap(in).getLong();
                break;
            case "float":
                result = ByteBuffer.wrap(in).getFloat();
                break;
            case "double":
                result = ByteBuffer.wrap(in).getDouble();
                break;
            default:
                System.err.println("Unknown type: " + clazz.getName());
        }

        return result;
    }

    private String getName(Method method) {
        StringBuilder sb = new StringBuilder(method.getDeclaringClass().getSimpleName());
        sb.append('.').append(method.getName());
        sb.append('(');
        StringJoiner sj = new StringJoiner(",");
        Class<?>[] var3 = method.getParameterTypes();

        for (Class<?> parameterType : var3) {
            sj.add(parameterType.getTypeName());
        }

        sb.append(sj);
        sb.append(')');
        return sb.toString();
    }
}
