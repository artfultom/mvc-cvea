package io.github.artfultom.vecenta.controller;

import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.generate.CodeGenerateStrategy;
import io.github.artfultom.vecenta.generate.DefaultCodeGenerateStrategy;
import io.github.artfultom.vecenta.generate.FileGenerator;
import io.github.artfultom.vecenta.generate.config.GenerateConfiguration;
import io.github.artfultom.vecenta.generate.config.GenerateMode;
import io.github.artfultom.vecenta.generated.v1.SumClient;
import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.Server;
import io.github.artfultom.vecenta.transport.error.MessageError;
import io.github.artfultom.vecenta.transport.tcp.TcpClient;
import io.github.artfultom.vecenta.transport.tcp.TcpServer;
import io.github.artfultom.vecenta.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ControllerTest {

    @Test
    public void testGeneration() throws IOException, URISyntaxException {
        URL schemaRes = getClass().getResource("/schema_generation");
        assertNotNull(schemaRes);

        Path schemaDir = Path.of(schemaRes.toURI());
        Path tempDir = Files.createTempDirectory("test_" + System.currentTimeMillis());

        CodeGenerateStrategy strategy = new DefaultCodeGenerateStrategy();
        GenerateConfiguration config = new GenerateConfiguration(
                schemaDir,
                tempDir,
                "test.pack.server",
                "test.pack.client"
        );

        List<Path> files = new FileGenerator(strategy).generateFiles(config);

        for (Path file : files) {
            String expectedFileName = file.getFileName().toString();

            URL expectedRes = getClass().getResource("/schema_generation/" + expectedFileName);
            assertNotNull(expectedRes);
            Path expected = Path.of(expectedRes.toURI());

            assertEquals(Files.readString(expected), Files.readString(file));

            Files.delete(file);
        }

        TestUtils.deleteDir(tempDir);
    }

    @Test
    public void testController() throws URISyntaxException, IOException, ProtocolException {
        CodeGenerateStrategy strategy = new DefaultCodeGenerateStrategy();

        URL res = getClass().getResource("/schema_controller");
        assertNotNull(res);

        String pack = "io.github.artfultom.vecenta.generated";
        GenerateConfiguration config = new GenerateConfiguration(
                Path.of(res.toURI()),
                Path.of("src", "test", "java"),
                pack,
                pack
        );

        List<Path> files = new FileGenerator(strategy).generateFiles(config);
        assertNotNull(files);
        assertEquals(2, files.size());

        ServerMatcher matcher = new ServerMatcher();
        matcher.register(pack);

        try (Server server = new TcpServer(); Client client = new TcpClient()) {
            int port = 5550;

            server.start(port, matcher);

            client.startConnection("127.0.0.1", port);
            SumClient clientConnector = new SumClient(client);
            int result = clientConnector.sum(3, 2);

            Assert.assertEquals(5, result);
        }
    }

    @Test
    public void testControllerFail() throws URISyntaxException, IOException {
        CodeGenerateStrategy strategy = new DefaultCodeGenerateStrategy();

        URL res = getClass().getResource("/schema_controller");
        assertNotNull(res);

        String pack = "io.github.artfultom.vecenta.generated";
        GenerateConfiguration config = new GenerateConfiguration(
                Path.of(res.toURI()),
                Path.of("src", "test", "java"),
                pack,
                pack,
                GenerateMode.CLIENT
        );

        List<Path> files = new FileGenerator(strategy).generateFiles(config);
        assertNotNull(files);
        assertEquals(1, files.size());

        try (Server server = new TcpServer(); Client client = new TcpClient()) {
            int port = 5550;

            server.start(port, new ServerMatcher());

            client.startConnection("127.0.0.1", port);
            SumClient clientConnector = new SumClient(client);
            clientConnector.sum(3, 2);

            Assert.fail();
        } catch (ProtocolException e) {
            Assert.assertEquals(MessageError.WRONG_METHOD_NAME, e.getError());
        }
    }
}