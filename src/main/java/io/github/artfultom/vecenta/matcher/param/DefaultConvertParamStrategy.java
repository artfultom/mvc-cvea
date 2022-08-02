package io.github.artfultom.vecenta.matcher.param;

import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.matcher.AbstractConvertParamStrategy;
import io.github.artfultom.vecenta.matcher.CollectionType;
import io.github.artfultom.vecenta.matcher.TypeConverter;
import io.github.artfultom.vecenta.matcher.annotations.Model;
import io.github.artfultom.vecenta.matcher.annotations.ModelField;
import io.github.artfultom.vecenta.util.ReflectionUtils;

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

    @Override
    public byte[] convertToByteArray(Object in) throws ConvertException {
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
                throw new ConvertException(
                        String.format("Cannot find an order of fields in model %s.", in.getClass().getName())
                );
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
            throw new ConvertException(String.format("Cannot open binary stream for type %s.", in.getClass().getName()), e);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ConvertException(String.format("Cannot invoke method. Type %s.", in.getClass().getName()), e);
        }
    }

    @Override
    public <T> T convertToObject( // TODO refactor
            byte[] in,
            String type,
            Class<T> target
    ) throws ConvertException {
        if (in.length == 0) {
            return null;
        }

        CollectionType collectionType = CollectionType.get(type);
        if (collectionType == null) {
            return null;
        }

        switch (collectionType) {
            case SIMPLE:
                TypeConverter converter = TypeConverter.get(type);
                if (converter != null) {
                    Object obj = converter.convert(in);

                    if (target.isInstance(obj)) {
                        return target.cast(obj);
                    } else {
                        throw new ConvertException(String.format("%s must be an instance of %s.", obj, target.getName()));
                    }
                }

                Model model = target.getAnnotation(Model.class);
                if (model == null) {
                    throw new ConvertException(
                            String.format("Cannot find an order of fields in model %s.", target.getName())
                    );
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
                    throw new ConvertException(String.format("Cannot invoke method. Type %s.", target.getName()), e);
                }
            case LIST:
                ByteBuffer listBuffer = ByteBuffer.wrap(in);

                int listSize = listBuffer.getInt();

                List<Object> resultList = new ArrayList<>(listSize);
                for (int i = 0; i < listSize; i++) {
                    int size = listBuffer.getInt();
                    byte[] listByteArray = new byte[size];
                    listBuffer.get(listByteArray);

                    Class<?> elementClass = null;
                    switch (CollectionType.get(collectionType.getFirst(type))) {
                        case SIMPLE:
                            elementClass = getElementClass(collectionType.getFirst(type));
                            break;
                        case LIST:
                            elementClass = List.class;
                            break;
                        case MAP:
                            elementClass = Map.class;
                            break;
                        default:
                    }

                    Object result = convertToObject(listByteArray, collectionType.getFirst(type), elementClass);
                    resultList.add(result);
                }

                if (target.isInstance(resultList)) {
                    return target.cast(resultList);
                } else {
                    throw new ConvertException(
                            String.format("%s must be an instance of %s.", resultList, target.getName())
                    );
                }
            case MAP:
                Class<?> mapKeyClass = getElementClass(collectionType.getFirst(type));
                if (mapKeyClass == null) {
                    throw new ConvertException(
                            String.format("Element class must not be null. Target=%s; type=%s.", target.getName(), type)
                    );
                }

                Class<?> mapValClass = getElementClass(collectionType.getSecond(type));
                if (mapValClass == null) {
                    CollectionType secondCollectionType = CollectionType.get(collectionType.getSecond(type));
                    if (secondCollectionType == null) {
                        throw new ConvertException(
                                String.format("Unknown CollectionType for %s.", collectionType.getSecond(type))
                        );
                    }

                    switch (secondCollectionType) {
                        case LIST:
                            mapValClass = List.class;
                            break;
                        case MAP:
                            mapValClass = Map.class;
                            break;
                        default:
                            throw new ConvertException(
                                    String.format("Unknown CollectionType for %s.", collectionType.getSecond(type))
                            );
                    }
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
                            convertToObject(keyArray, collectionType.getFirst(type), mapKeyClass),
                            convertToObject(valArray, collectionType.getSecond(type), mapValClass)
                    );
                }

                if (target.isInstance(resultMap)) {
                    return target.cast(resultMap);
                } else {
                    throw new ConvertException(
                            String.format("%s must be an instance of %s.", resultMap, target.getName())
                    );
                }
            default:
                throw new ConvertException(String.format("Unknown type of CollectionType - %s.", collectionType));
        }
    }

    private Class<?> getElementClass(String type) {
        TypeConverter typeConverter = TypeConverter.get(type);

        if (typeConverter == null) {
            return models.get(type);
        } else {
            return typeConverter.getClazz();
        }
    }
}
