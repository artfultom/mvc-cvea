package io.github.artfultom.vecenta.matcher;

import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.matcher.annotations.RpcError;
import io.github.artfultom.vecenta.matcher.annotations.RpcMethod;
import io.github.artfultom.vecenta.matcher.param.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.param.DefaultConvertParamStrategy;
import io.github.artfultom.vecenta.transport.MethodHandler;
import io.github.artfultom.vecenta.transport.error.ErrorType;
import io.github.artfultom.vecenta.transport.message.Request;
import io.github.artfultom.vecenta.transport.message.Response;
import io.github.artfultom.vecenta.util.ReflectionUtils;
import io.github.artfultom.vecenta.util.StringUtils;
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
            Set<Class<?>> classes = ReflectionUtils.findServerClasses(pack);

            for (Class<?> clazz : classes) {
                register(clazz);
            }
        } catch (IOException e) {
            log.error(String.format("Cannot register a server class in package %s.", pack), e);
        }
    }

    public void register(Class<?> serverClass) {
        for (Method method : serverClass.getDeclaredMethods()) {
            Method interfaceMethod = getInterfaceMethod(method);

            if (interfaceMethod != null) {
                RpcMethod rpcMethod = interfaceMethod.getAnnotation(RpcMethod.class);

                if (rpcMethod != null) {
                    String name = StringUtils.getMethodName(
                            rpcMethod.entity(),
                            rpcMethod.name(),
                            Arrays.asList(rpcMethod.argumentTypes()),
                            rpcMethod.returnType()
                    );
                    MethodHandler handler = new MethodHandler(name, request -> {
                        try {
                            List<Object> requestParams = new ArrayList<>();
                            for (int i = 0; i < request.getParams().size(); i++) {
                                byte[] param = request.getParams().get(i);

                                requestParams.add(convertParamStrategy.convertToObject(
                                        param,
                                        rpcMethod.argumentTypes()[i],
                                        method.getParameterTypes()[i]
                                ));
                            }

                            Object result = method.invoke(
                                    serverClass.getDeclaredConstructor().newInstance(),
                                    requestParams.toArray()
                            );

                            byte[] responseParam = convertParamStrategy.convertToByteArray(result);

                            return new Response(responseParam);
                        } catch (
                                IllegalAccessException | InstantiationException | NoSuchMethodException |
                                ConvertException e
                        ) {
                            log.error(String.format("Cannot register a server class %s.", serverClass.getName()), e);
                        } catch (InvocationTargetException e) {
                            Throwable target = e.getTargetException();
                            RpcError error = target.getClass().getAnnotation(RpcError.class);
                            if (error == null) {
                                return new Response(ErrorType.UNKNOWN_METHOD_ERROR);
                            }

                            return new Response(ErrorType.CHECKED_ERROR, error.name());
                        }

                        return new Response(ErrorType.WRONG_METHOD_NAME);
                    });

                    register(handler);
                }
            }
        }
    }

    public void register(MethodHandler handler) {
        handlerMap.put(handler.getName(), handler);
    }

    public byte[] process(byte[] in) {
        Request request = readWriteStrategy.convertToRequest(in);

        MethodHandler handler = handlerMap.get(request.getMethodName());
        if (handler == null) {
            return readWriteStrategy.convertToBytes(new Response(ErrorType.WRONG_METHOD_NAME));
        }

        Response response = handler.execute(request);

        return readWriteStrategy.convertToBytes(response);
    }

    private Method getInterfaceMethod(Method method) {
        List<Method> methods = Stream.of(method.getDeclaringClass().getInterfaces())
                .map(Class::getMethods)
                .flatMap(Arrays::stream)
                .filter(item -> item.getName().equals(method.getName()))
                .filter(item -> Arrays.equals(item.getParameterTypes(), method.getParameterTypes()))
                .filter(item -> item.getAnnotation(RpcMethod.class) != null)
                .collect(Collectors.toList());

        if (methods.isEmpty()) {
            return null;
        }
        if (methods.size() > 1) {
            log.warn(String.format("Too many methods with name \"%s\". count=%d", method.getName(), methods.size()));
        }

        return methods.get(0);
    }
}
