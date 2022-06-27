package io.github.artfultom.vecenta.generate;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.artfultom.vecenta.exceptions.ValidateException;
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
    public void isCorrectFileName() throws ValidateException {
        strategy.check("Name.1.json");

        ValidateException ex1 = Assert.assertThrows(ValidateException.class, () -> strategy.check((String) null));
        Assert.assertEquals("File name is empty.", ex1.getMessage());

        ValidateException ex2 = Assert.assertThrows(ValidateException.class, () -> strategy.check(""));
        Assert.assertEquals("File name is empty.", ex2.getMessage());

        ValidateException ex3 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.json"));
        Assert.assertEquals("Incorrect file name: Name.json. It must have tree parts.", ex3.getMessage());

        ValidateException ex4 = Assert.assertThrows(ValidateException.class, () -> strategy.check(".1.json"));
        Assert.assertEquals("Incorrect file name: .1.json. Server name is empty.", ex4.getMessage());

        ValidateException ex5 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name..json"));
        Assert.assertEquals("Incorrect file name: Name..json. Version is empty.", ex5.getMessage());

        ValidateException ex6 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.1."));
        Assert.assertEquals("Incorrect file name: Name.1.. It must have tree parts.", ex6.getMessage());

        ValidateException ex7 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.1.test"));
        Assert.assertEquals("Incorrect file name: Name.1.test. It must be json.", ex7.getMessage());

        ValidateException ex8 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.A.json"));
        Assert.assertEquals("Incorrect file name: Name.A.json. Version is incorrect.", ex8.getMessage());
    }

    @Test
    public void isCorrectDto() throws URISyntaxException, IOException, ValidateException {
        URL res = getClass().getResource("/validation/Server.1.json");
        Assert.assertNotNull(res);

        Path file = Path.of(res.toURI());
        String body = Files.readString(file);
        Assert.assertNotNull(body);

        ObjectMapper mapper = new ObjectMapper();
        JsonFormatDto dto = mapper.readValue(body, JsonFormatDto.class);
        Assert.assertNotNull(dto);

        strategy.check(dto);
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

        ValidateException ex = Assert.assertThrows(ValidateException.class, () -> strategy.check(dto));
        Assert.assertEquals("Incorrect argument type WRONG_MODEL_NAME of method method.", ex.getMessage());
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

        ValidateException ex = Assert.assertThrows(ValidateException.class, () -> strategy.check(dto));
        Assert.assertEquals("There is a circle!", ex.getMessage());
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

        ValidateException ex = Assert.assertThrows(ValidateException.class, () -> strategy.check(dto));
        Assert.assertEquals("Duplicates of models.", ex.getMessage());
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

        ValidateException ex = Assert.assertThrows(ValidateException.class, () -> strategy.check(dto));
        Assert.assertEquals("Incorrect return type WRONG_MODEL_NAME of method method.", ex.getMessage());
    }
}