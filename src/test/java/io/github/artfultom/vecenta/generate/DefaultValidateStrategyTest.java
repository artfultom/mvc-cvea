package io.github.artfultom.vecenta.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class DefaultValidateStrategyTest {

    private final DefaultValidateStrategy strategy = new DefaultValidateStrategy();

    @Test
    public void isCorrectFileName() {
        Assert.assertTrue(strategy.isCorrect("Name.1.json"));
        Assert.assertFalse(strategy.isCorrect((String) null));
        Assert.assertFalse(strategy.isCorrect(""));
        Assert.assertFalse(strategy.isCorrect("Name.json"));
        Assert.assertFalse(strategy.isCorrect(".1.json"));
        Assert.assertFalse(strategy.isCorrect("Name..json"));
        Assert.assertFalse(strategy.isCorrect("Name.1."));
        Assert.assertFalse(strategy.isCorrect("Name.1.test"));
        Assert.assertFalse(strategy.isCorrect("Name.A.json"));
    }

    @Test
    public void isCorrectDto() throws URISyntaxException, IOException {
        URL res = getClass().getResource("/validation/Server.1.json");
        Assert.assertNotNull(res);

        Path file = Path.of(res.toURI());
        String body = Files.readString(file);
        Assert.assertNotNull(body);

        ObjectMapper mapper = new ObjectMapper();
        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);
        Assert.assertNotNull(dto);

        Assert.assertTrue(strategy.isCorrect(dto));
    }

    @Test
    public void isCorrectDtoWrongModel() throws URISyntaxException, IOException {
        URL res = getClass().getResource("/validation/Server_wrong_model.1.json");
        Assert.assertNotNull(res);

        Path file = Path.of(res.toURI());
        String body = Files.readString(file);
        Assert.assertNotNull(body);

        ObjectMapper mapper = new ObjectMapper();
        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);
        Assert.assertNotNull(dto);

        Assert.assertFalse(strategy.isCorrect(dto));
    }

    @Test
    public void isCorrectDtoWrongRecursion() throws URISyntaxException, IOException {
        URL res = getClass().getResource("/validation/Server_wrong_recursion.1.json");
        Assert.assertNotNull(res);

        Path file = Path.of(res.toURI());
        String body = Files.readString(file);
        Assert.assertNotNull(body);

        ObjectMapper mapper = new ObjectMapper();
        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);
        Assert.assertNotNull(dto);

        Assert.assertFalse(strategy.isCorrect(dto));
    }

    @Test
    public void isCorrectDtoWrongDuplicates() throws URISyntaxException, IOException {
        URL res = getClass().getResource("/validation/Server_wrong_duplicates.1.json");
        Assert.assertNotNull(res);

        Path file = Path.of(res.toURI());
        String body = Files.readString(file);
        Assert.assertNotNull(body);

        ObjectMapper mapper = new ObjectMapper();
        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);
        Assert.assertNotNull(dto);

        Assert.assertFalse(strategy.isCorrect(dto));
    }

    @Test
    public void isCorrectDtoWrongReturn() throws URISyntaxException, IOException {
        URL res = getClass().getResource("/validation/Server_wrong_return.1.json");
        Assert.assertNotNull(res);

        Path file = Path.of(res.toURI());
        String body = Files.readString(file);
        Assert.assertNotNull(body);

        ObjectMapper mapper = new ObjectMapper();
        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);
        Assert.assertNotNull(dto);

        Assert.assertFalse(strategy.isCorrect(dto));
    }
}