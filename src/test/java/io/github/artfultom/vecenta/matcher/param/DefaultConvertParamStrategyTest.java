package io.github.artfultom.vecenta.matcher.param;

import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.generated.v1.math.Model3;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class DefaultConvertParamStrategyTest {

    @Test
    public void convertToByteArray() throws ConvertException {
        ConvertParamStrategy strategy = new DefaultConvertParamStrategy();

        byte[] result1 = strategy.convertToByteArray(1);
        Assert.assertArrayEquals(new byte[]{0, 0, 0, 1}, result1);

        byte[] result2 = strategy.convertToByteArray("TEST");
        Assert.assertArrayEquals("TEST".getBytes(StandardCharsets.UTF_8), result2);

        Model3 model3 = new Model3();
        model3.setField1(1);

        byte[] result3 = strategy.convertToByteArray(model3);
        ByteBuffer bb3 = ByteBuffer.wrap(result3);
        Assert.assertEquals(4, bb3.getInt());
        Assert.assertEquals(1, bb3.getInt());
        Assert.assertEquals(0, bb3.getInt());
        Assert.assertEquals(0, bb3.getInt());
        Assert.assertEquals(0, bb3.getInt());

        byte[] result4 = strategy.convertToByteArray(List.of(1));
        ByteBuffer bb4 = ByteBuffer.wrap(result4);
        Assert.assertEquals(1, bb4.getInt());
        Assert.assertEquals(Integer.BYTES, bb4.getInt());
        Assert.assertEquals(1, bb4.getInt());

        byte[] result5 = strategy.convertToByteArray(List.of(model3));
        ByteBuffer bb5 = ByteBuffer.wrap(result5);
        Assert.assertEquals(1, bb5.getInt());
        Assert.assertEquals(20, bb5.getInt());
        Assert.assertEquals(4, bb5.getInt());
        Assert.assertEquals(1, bb5.getInt());
        Assert.assertEquals(0, bb5.getInt());
        Assert.assertEquals(0, bb5.getInt());
        Assert.assertEquals(0, bb5.getInt());

        byte[] result6 = strategy.convertToByteArray(Map.of(1, true));
        ByteBuffer bb6 = ByteBuffer.wrap(result6);
        Assert.assertEquals(1, bb6.getInt());
        Assert.assertEquals(Integer.BYTES, bb6.getInt());
        Assert.assertEquals(1, bb6.getInt());
        Assert.assertEquals(Byte.BYTES, bb6.getInt());
        Assert.assertEquals(1, bb6.get());

        byte[] result9 = strategy.convertToByteArray(Map.of(1, model3));
        ByteBuffer bb9 = ByteBuffer.wrap(result9);
        Assert.assertEquals(1, bb9.getInt());
        Assert.assertEquals(4, bb9.getInt());
        Assert.assertEquals(1, bb9.getInt());
        Assert.assertEquals(20, bb9.getInt());
        Assert.assertEquals(4, bb9.getInt());
        Assert.assertEquals(1, bb9.getInt());
        Assert.assertEquals(0, bb9.getInt());
        Assert.assertEquals(0, bb9.getInt());
        Assert.assertEquals(0, bb9.getInt());
    }

    @Test
    public void convertToObject() throws ConvertException {
        ConvertParamStrategy strategy = new DefaultConvertParamStrategy();

        byte[] val1 = strategy.convertToByteArray(1);
        int result1 = strategy.convertToObject(val1, "int32", Integer.class);
        Assert.assertEquals(1, result1);

        byte[] val2 = strategy.convertToByteArray("TEST");
        String result2 = strategy.convertToObject(val2, "string", String.class);
        Assert.assertEquals("TEST", result2);

        Model3 model3 = new Model3();
        model3.setField1(1);

        byte[] val3 = strategy.convertToByteArray(model3);
        Model3 result3 = strategy.convertToObject(val3, "TestClient.math.Model3", Model3.class);
        Assert.assertEquals(model3.getField1(), result3.getField1());

        byte[] val4 = strategy.convertToByteArray(List.of(1));
        List<Integer> result4 = strategy.convertToObject(val4, "[int32]", List.class);
        Assert.assertEquals(List.of(1), result4);

        byte[] val5 = strategy.convertToByteArray(List.of(List.of(1)));
        List<List<Integer>> result5 = strategy.convertToObject(val5, "[[int32]]", List.class);
        Assert.assertEquals(List.of(List.of(1)), result5);

        byte[] val6 = strategy.convertToByteArray(List.of(model3));
        List<Integer> result6 = strategy.convertToObject(val6, "[TestClient.math.Model3]", List.class);
        Assert.assertEquals(1, result6.size());

        byte[] val7 = strategy.convertToByteArray(Map.of(1, true));
        Map<Integer, Boolean> result7 = strategy.convertToObject(val7, "[int32]boolean", Map.class);
        Assert.assertEquals(Map.of(1, true), result7);

        byte[] val8 = strategy.convertToByteArray(Map.of(1, model3));
        Map<Integer, Boolean> result8 = strategy.convertToObject(val8, "[int32]TestClient.math.Model3", Map.class);
        Assert.assertEquals(1, result8.size());
    }
}