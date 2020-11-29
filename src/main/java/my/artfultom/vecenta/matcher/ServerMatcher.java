package my.artfultom.vecenta.matcher;

import my.artfultom.vecenta.transport.MethodHandler;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ServerMatcher {

    private ReadWriteStrategy readWriteStrategy;
    private ConvertParamStrategy convertParamStrategy;
    private final Map<String, MethodHandler> handlerMap = new HashMap<>();

    public ServerMatcher() {
        this.readWriteStrategy = new DefaultReadWriteStrategy();
        this.convertParamStrategy = new DefaultConvertParamStrategy();
    }

    public void setReadWriteStrategy(ReadWriteStrategy readWriteStrategy) {
        this.readWriteStrategy = readWriteStrategy;
    }

    public void setConvertParamStrategy(ConvertParamStrategy convertParamStrategy) {
        this.convertParamStrategy = convertParamStrategy;
    }

    public void register(Class<?> controllerClass) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            MethodHandler handler = new MethodHandler(getName(method), (request) -> {
                try {
                    Class<?> returnType = method.getReturnType();

                    List<Object> requestParams = new ArrayList<>();
                    for (int i = 0; i < request.getParams().size(); i++) {
                        byte[] param = request.getParams().get(i);

                        requestParams.add(convertParamStrategy.convertToObject(method.getParameterTypes()[i], param));
                    }

                    Object result = method.invoke( // TODO static methods and others constructors
                            controllerClass.getDeclaredConstructor().newInstance(),
                            requestParams.toArray()
                    );

                    List<byte[]> responseParams = List.of(convertParamStrategy.convertToByteArray(returnType, result));

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

                return new Response(1); // TODO code
            });

            register(handler);
        }
    }

    public void register(MethodHandler handler) { // TODO bool?
        handlerMap.put(handler.getName(), handler);
    }

    public byte[] process(byte[] in) {
        Request request = readWriteStrategy.convertToRequest(in);

        MethodHandler handler = handlerMap.get(request.getMethodName());
        if (handler == null) {
            return readWriteStrategy.convertToBytes(new Response(666));
        }

        Response response = handler.execute(request);

        return readWriteStrategy.convertToBytes(response);
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
