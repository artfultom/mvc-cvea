package io.github.artfultom.vecenta.controller;

import io.github.artfultom.vecenta.generate.CodeGenerateStrategy;
import io.github.artfultom.vecenta.generate.DefaultCodeGenerateStrategy;
import io.github.artfultom.vecenta.generate.FileGenerator;
import io.github.artfultom.vecenta.generate.config.GenerateConfiguration;
import io.github.artfultom.vecenta.generated.v1.SumClient;
import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.Client;
import io.github.artfultom.vecenta.transport.Server;
import io.github.artfultom.vecenta.transport.tcp.TcpClient;
import io.github.artfultom.vecenta.transport.tcp.TcpServer;
import io.github.artfultom.vecenta.utils.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
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
    public void testController() throws URISyntaxException, IOException, ClassNotFoundException {
        CodeGenerateStrategy strategy = new DefaultCodeGenerateStrategy();

        URL res = getClass().getResource("/schema_controller");
        assertNotNull(res);
        GenerateConfiguration config = new GenerateConfiguration(
                Path.of(res.toURI()),
                Path.of("src", "test", "java"),
                "io.github.artfultom.vecenta.generated",
                "io.github.artfultom.vecenta.generated"
        );

        List<Path> files = new FileGenerator(strategy).generateFiles(config);

        ServerMatcher matcher = new ServerMatcher();

        // TODO find
        ClassLoader cl = new URLClassLoader(new URL[]{files.get(0).getParent().toUri().toURL()});
        Class<?> serverClass = cl.loadClass("io.github.artfultom.vecenta.generated.v1.SumServerImpl");
        Class<?> clientClass = cl.loadClass("io.github.artfultom.vecenta.generated.v1.SumClient");
        matcher.register(serverClass);

        try (Server server = new TcpServer(); Client client = new TcpClient()) {
            server.start(5550, matcher);

            client.startConnection("127.0.0.1", 5550);
            SumClient clientConnector = new SumClient(client);
            int result = clientConnector.sum(3, 2);

            Assert.assertEquals(5, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}