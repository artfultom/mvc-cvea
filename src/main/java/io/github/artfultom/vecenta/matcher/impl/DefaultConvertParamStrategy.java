package io.github.artfultom.vecenta.matcher.impl;

import io.github.artfultom.vecenta.matcher.ConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.Model;
import io.github.artfultom.vecenta.matcher.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultConvertParamStrategy implements ConvertParamStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultConvertParamStrategy.class);

    @Override
    public byte[] convertToByteArray(Object in) {
        byte[] result = null;

        TypeConverter converter = TypeConverter.get(in.getClass());
        if (converter == null) {
            Model model = in.getClass().getAnnotation(Model.class);
            if (model == null) {
                log.error("Cannot find an order of fields in model " + in.getClass().getName());
                return new byte[0];
            }

            Map<String, Method> methodMap = Arrays.stream(in.getClass().getDeclaredMethods())
                    .filter(item -> item.getName().startsWith("get") && item.getParameterTypes().length == 0)
                    .filter(item -> Modifier.isPublic(item.getModifiers()))
                    .collect(Collectors.toMap(
                            item -> item.getName().replace("get", "").toLowerCase(),
                            item -> item
                    ));

            List<Method> methods = Arrays.stream(model.order())
                    .map(item -> methodMap.get(item.toLowerCase()))
                    .collect(Collectors.toList());

            try (
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DataOutputStream dataStream = new DataOutputStream(out)
            ) {
                for (Method method : methods) {
                    Object val = method.invoke(in);
                    byte[] bytes = convertToByteArray(val);

                    dataStream.writeInt(bytes.length);
                    dataStream.write(bytes);
                }

                result = out.toByteArray();
            } catch (IOException e) {
                log.error("Cannot open binary stream for type " + in.getClass().getName(), e);
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("Cannot invoke method. Type " + in.getClass().getName(), e);
            }
        } else {
            result = converter.convert(in);
        }

        return result;
    }

    @Override
    public <T> T convertToObject(Class<T> clazz, byte[] in) {
        T result = null;

        TypeConverter converter = TypeConverter.get(clazz);
        if (converter == null) {
            Model model = clazz.getAnnotation(Model.class);
            if (model == null) {
                log.error("Cannot find an order of fields in model " + clazz.getName());
                return null;
            }

            Map<String, Method> methodMap = Arrays.stream(clazz.getDeclaredMethods())
                    .filter(item -> item.getName().startsWith("set") && item.getParameterTypes().length == 1)
                    .filter(item -> Modifier.isPublic(item.getModifiers()))
                    .collect(Collectors.toMap(
                            item -> item.getName().replace("set", "").toLowerCase(),
                            item -> item
                    ));

            List<Method> methods = Arrays.stream(model.order())
                    .map(item -> methodMap.get(item.toLowerCase()))
                    .collect(Collectors.toList());

            try {
                T obj = clazz.getDeclaredConstructor().newInstance();

                ByteBuffer buf = ByteBuffer.wrap(in);
                for (Method method : methods) {
                    int size = buf.getInt();
                    byte[] dst = new byte[size];
                    buf.get(dst);

                    Class<?> type = method.getParameterTypes()[0];
                    method.invoke(obj, convertToObject(type, dst));
                }

                result = obj;
            } catch (
                    InvocationTargetException |
                    InstantiationException |
                    IllegalAccessException |
                    NoSuchMethodException e
            ) {
                log.error("Cannot invoke method. Type " + clazz.getName(), e);
            }
        } else {
            result = (T) converter.convert(in);
        }

        return result;
    }
}
