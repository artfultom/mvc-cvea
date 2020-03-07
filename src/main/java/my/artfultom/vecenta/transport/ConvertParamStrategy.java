package my.artfultom.vecenta.transport;

public interface ConvertParamStrategy {

    byte[] convertToByteArray(Class<?> clazz, Object in);

    Object convertToObject(Class<?> clazz, byte[] in);
}
