package io.github.artfultom.vecenta.matcher.impl;

import io.github.artfultom.vecenta.matcher.*;
import io.github.artfultom.vecenta.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultConvertParamStrategy extends AbstractConvertParamStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultConvertParamStrategy.class);

    @Override
    public byte[] convertToByteArray(Object in) {
        if (in == null) {
            return new byte[0];
        }

        try (
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(out)
        ) {
            if (List.class.isAssignableFrom(in.getClass())) {
                List<?> list = (List<?>) in;

                dataStream.writeInt(list.size());

                for (Object obj : list) {
                    byte[] bytes = convertToByteArray(obj);

                    dataStream.writeInt(bytes.length);
                    dataStream.write(bytes);
                }

                return out.toByteArray();
            }

            if (Map.class.isAssignableFrom(in.getClass())) {
                Map<?, ?> map = (Map<?, ?>) in;

                dataStream.writeInt(map.size());

                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    byte[] keyBytes = convertToByteArray(entry.getKey());

                    dataStream.writeInt(keyBytes.length);
                    dataStream.write(keyBytes);

                    byte[] valBytes = convertToByteArray(entry.getValue());

                    dataStream.writeInt(valBytes.length);
                    dataStream.write(valBytes);
                }

                return out.toByteArray();
            }

            TypeConverter converter = TypeConverter.get(in.getClass());
            if (converter != null) {
                return converter.convert(in);
            }

            Model model = in.getClass().getAnnotation(Model.class);
            if (model == null) {
                log.error("Cannot find an order of fields in model " + in.getClass().getName());
                return new byte[0];
            }

            Map<String, Method> methodMap = ReflectionUtils.getPublicGetters(in.getClass()).stream()
                    .collect(Collectors.toMap(
                            item -> item.getName().replace("get", "").toLowerCase(),
                            item -> item
                    ));

            List<Method> methods = Arrays.stream(model.order())
                    .map(item -> methodMap.get(item.toLowerCase()))
                    .collect(Collectors.toList());

            for (Method method : methods) {
                Object val = method.invoke(in);
                byte[] bytes = convertToByteArray(val);

                dataStream.writeInt(bytes.length);
                dataStream.write(bytes);
            }

            return out.toByteArray();
        } catch (IOException e) {
            log.error("Cannot open binary stream for type " + in.getClass().getName(), e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            log.error("Cannot invoke method. Type " + in.getClass().getName(), e);
        }

        return new byte[0];
    }

    @Override
    public <T> T convertToObject(byte[] in, String type, Class<T> target) {
        if (in.length == 0) {
            return null;
        }

        CollectionType collectionType = CollectionType.get(type);
        if (collectionType == null) {
            return null;
        }

        Class<?> firstElementClass = getElementClass(collectionType.getFirst());
        Class<?> secondElementClass = getElementClass(collectionType.getSecond());

        switch (collectionType) {
            case SIMPLE:
                TypeConverter converter = TypeConverter.get(type);
                if (converter != null) {
                    return (T) converter.convert(in);
                }

                Model model = target.getAnnotation(Model.class);
                if (model == null) {
                    log.error("Cannot find an order of fields in model " + target.getName());
                    return null;
                }

                Map<String, Method> methodMap = ReflectionUtils.getPublicSetters(target).stream()
                        .collect(Collectors.toMap(
                                item -> item.getName().replace("set", "").toLowerCase(),
                                item -> item
                        ));

                List<Method> methods = Arrays.stream(model.order())
                        .map(item -> methodMap.get(item.toLowerCase()))
                        .collect(Collectors.toList());

                try {
                    T obj = target.getDeclaredConstructor().newInstance();

                    ByteBuffer buf = ByteBuffer.wrap(in);
                    for (Method method : methods) {
                        int size = buf.getInt();
                        byte[] dst = new byte[size];
                        buf.get(dst);

                        Field field = ReflectionUtils.getField(target, method);
                        ModelField modelField = field.getAnnotation(ModelField.class);

                        Class<?> parameterType = method.getParameterTypes()[0];
                        method.invoke(obj, convertToObject(dst, modelField.type(), parameterType));
                    }

                    return obj;
                } catch (
                        InvocationTargetException |
                        InstantiationException |
                        IllegalAccessException |
                        NoSuchMethodException e
                ) {
                    log.error("Cannot invoke method. Type " + target.getName(), e);
                }

                break;
            case LIST:
                if (firstElementClass == null) {
                    log.error("Element class must not be null. Target=" + target.getName() + "; type=" + type);
                }

                ByteBuffer listBuffer = ByteBuffer.wrap(in);

                int listSize = listBuffer.getInt();

                List<Object> resultList = new ArrayList<>(listSize);
                for (int i = 0; i < listSize; i++) {
                    int size = listBuffer.getInt();
                    byte[] listByteArray = new byte[size];
                    listBuffer.get(listByteArray);

                    resultList.add(convertToObject(listByteArray, collectionType.getFirst(), firstElementClass));
                }

                return (T) resultList;
            case MAP:
                if (firstElementClass == null || secondElementClass == null) {
                    log.error("Element class must not be null. Target=" + target.getName() + "; type=" + type);
                }

                ByteBuffer mapBuffer = ByteBuffer.wrap(in);

                int mapSize = mapBuffer.getInt();

                Map<Object, Object> resultMap = new HashMap<>(mapSize);
                for (int i = 0; i < mapSize; i++) {
                    int keySize = mapBuffer.getInt();
                    byte[] keyArray = new byte[keySize];
                    mapBuffer.get(keyArray);

                    int valSize = mapBuffer.getInt();
                    byte[] valArray = new byte[valSize];
                    mapBuffer.get(valArray);

                    resultMap.put(
                            convertToObject(keyArray, collectionType.getFirst(), firstElementClass),
                            convertToObject(valArray, collectionType.getSecond(), secondElementClass)
                    );
                }

                return (T) resultMap;
            default:
                log.error("Unknown type of CollectionType - " + collectionType);
        }

        return null;
    }

    private Class<?> getElementClass(String type) {
        TypeConverter firstTypeConverter = TypeConverter.get(type);

        if (firstTypeConverter == null) {
            return models.get(type);
        } else {
            return firstTypeConverter.getClazz();
        }
    }
}
