package com.github.artfultom.vecenta.matcher;

import com.github.artfultom.vecenta.transport.MethodHandler;
import com.github.artfultom.vecenta.transport.error.MessageError;
import com.github.artfultom.vecenta.transport.message.Request;
import com.github.artfultom.vecenta.transport.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerMatcher {

    private static final Logger log = LoggerFactory.getLogger(ServerMatcher.class);

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
                } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                    log.error("cannot register a controller " + controllerClass.getName(), e);
                }

                return new Response(MessageError.WRONG_METHOD_NAME);
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
            return readWriteStrategy.convertToBytes(new Response(MessageError.WRONG_METHOD_NAME));
        }

        Response response = handler.execute(request);

        return readWriteStrategy.convertToBytes(response);
    }

    private String getName(Method method) {
        List<Method> methods = Stream.of(method.getDeclaringClass().getInterfaces())
                .map(Class::getMethods)
                .flatMap(Arrays::stream)
                .filter(item -> item.getName().equals(method.getName()))
                .collect(Collectors.toList());

        if (methods.size() == 0) {
            log.error("no methods with name \"" + method.getName() + "\"");
            // TODO return
        }
        if (methods.size() > 1) {
            log.warn("too many methods with name \"" + method.getName() + "\". count=" + methods.size());
        }
        Entity entity = methods.get(0).getAnnotation(Entity.class);

        StringBuilder sb = new StringBuilder(entity.value());
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
