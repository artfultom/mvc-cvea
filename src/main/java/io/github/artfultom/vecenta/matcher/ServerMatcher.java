package io.github.artfultom.vecenta.matcher;

import io.github.artfultom.vecenta.matcher.impl.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.impl.DefaultReadWriteStrategy;
import io.github.artfultom.vecenta.transport.MethodHandler;
import io.github.artfultom.vecenta.transport.error.MessageError;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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

    public void register(String pack) {
        try {
            List<Class<?>> classes = ReflectionUtils.findServerClasses(pack);

            for (Class<?> clazz : classes) {
                register(clazz);
            }
        } catch (IOException e) {
            log.error("Cannot register a controller in package " + pack, e);
        }
    }

    public void register(Class<?> controllerClass) {
        for (Method method : controllerClass.getDeclaredMethods()) {
            String name = getName(method);

            if (name == null) {
                continue;
            }

            MethodHandler handler = new MethodHandler(name, (request) -> {
                try {
                    Class<?> returnType = method.getReturnType();

                    List<Object> requestParams = new ArrayList<>();
                    for (int i = 0; i < request.getParams().size(); i++) {
                        byte[] param = request.getParams().get(i);

                        requestParams.add(convertParamStrategy.convertToObject(method.getParameterTypes()[i], param));
                    }

                    Object result = method.invoke(
                            controllerClass.getDeclaredConstructor().newInstance(),
                            requestParams.toArray()
                    );

                    byte[] responseParam = convertParamStrategy.convertToByteArray(returnType, result);

                    return new Response(responseParam);
                } catch (
                        IllegalAccessException |
                        InstantiationException |
                        NoSuchMethodException |
                        InvocationTargetException e
                ) {
                    log.error("Cannot register a controller " + controllerClass.getName(), e);
                }

                return new Response(MessageError.WRONG_METHOD_NAME);
            });

            register(handler);
        }
    }

    public void register(MethodHandler handler) {
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
            log.error("No methods with name \"" + method.getName() + "\"");

            return null;
        }
        if (methods.size() > 1) {
            log.warn("Too many methods with name \"" + method.getName() + "\". count=" + methods.size());
        }
        RpcMethod rpcMethod = methods.get(0).getAnnotation(RpcMethod.class);

        return rpcMethod.name();
    }
}
