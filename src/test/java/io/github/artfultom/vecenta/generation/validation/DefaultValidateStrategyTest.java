package io.github.artfultom.vecenta.generation.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.artfultom.vecenta.exceptions.ValidateException;
import io.github.artfultom.vecenta.generation.Data;
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
        strategy.check("Name.1.yml");

        ValidateException ex1 = Assert.assertThrows(ValidateException.class, () -> strategy.check((String) null));
        Assert.assertEquals("File name is empty.", ex1.getMessage());

        ValidateException ex2 = Assert.assertThrows(ValidateException.class, () -> strategy.check(""));
        Assert.assertEquals("File name is empty.", ex2.getMessage());

        ValidateException ex31 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.json"));
        Assert.assertEquals("Incorrect file name: Name.json. It must have tree parts.", ex31.getMessage());

        ValidateException ex32 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.yml"));
        Assert.assertEquals("Incorrect file name: Name.yml. It must have tree parts.", ex32.getMessage());

        ValidateException ex41 = Assert.assertThrows(ValidateException.class, () -> strategy.check(".1.json"));
        Assert.assertEquals("Incorrect file name: .1.json. Server name is empty.", ex41.getMessage());

        ValidateException ex42 = Assert.assertThrows(ValidateException.class, () -> strategy.check(".1.yml"));
        Assert.assertEquals("Incorrect file name: .1.yml. Server name is empty.", ex42.getMessage());

        ValidateException ex51 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name..json"));
        Assert.assertEquals("Incorrect file name: Name..json. Version is empty.", ex51.getMessage());

        ValidateException ex52 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name..yml"));
        Assert.assertEquals("Incorrect file name: Name..yml. Version is empty.", ex52.getMessage());

        ValidateException ex6 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.1."));
        Assert.assertEquals("Incorrect file name: Name.1.. It must have tree parts.", ex6.getMessage());

        ValidateException ex7 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.1.test"));
        Assert.assertEquals("Incorrect file name: Name.1.test. It must be json or yml.", ex7.getMessage());

        ValidateException ex81 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.A.json"));
        Assert.assertEquals("Incorrect file name: Name.A.json. Version is incorrect.", ex81.getMessage());

        ValidateException ex82 = Assert.assertThrows(ValidateException.class, () -> strategy.check("Name.A.yml"));
        Assert.assertEquals("Incorrect file name: Name.A.yml. Version is incorrect.", ex82.getMessage());
    }

    @Test
    public void isCorrectData() throws URISyntaxException, IOException, ValidateException {
        URL res = getClass().getResource("/validation/Server.1.json");
        Assert.assertNotNull(res);

        Path file = Path.of(res.toURI());
        String body = Files.readString(file);
        Assert.assertNotNull(body);

        ObjectMapper mapper = new ObjectMapper();
        Data data = mapper.readValue(body, Data.class);
        Assert.assertNotNull(data);

        strategy.check(data);
    }

    @Test
    public void isCorrectWrongData() throws URISyntaxException, IOException {
        checkWrongData("/validation/Server_wrong_model.1.json", "Incorrect type WRONG_MODEL_NAME.");
        checkWrongData("/validation/Server_wrong_recursion.1.json", "There is a circle!");
        checkWrongData("/validation/Server_wrong_duplicates.1.json", "Duplicates of models.");
        checkWrongData("/validation/Server_wrong_return.1.json", "Incorrect type WRONG_MODEL_NAME.");
    }

    private void checkWrongData(String filename, String errorMsg) throws URISyntaxException, IOException {
        URL res = getClass().getResource(filename);
        Assert.assertNotNull(res);

        Path file = Path.of(res.toURI());
        String body = Files.readString(file);
        Assert.assertNotNull(body);

        ObjectMapper mapper = new ObjectMapper();
        Data data = mapper.readValue(body, Data.class);
        Assert.assertNotNull(data);

        ValidateException ex = Assert.assertThrows(ValidateException.class, () -> strategy.check(data));
        Assert.assertEquals(errorMsg, ex.getMessage());
    }
}