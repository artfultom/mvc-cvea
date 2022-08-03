package io.github.artfultom.vecenta.server;

import io.github.artfultom.vecenta.exceptions.ConnectionException;
import io.github.artfultom.vecenta.exceptions.ConvertException;
import io.github.artfultom.vecenta.exceptions.ProtocolException;
import io.github.artfultom.vecenta.generated.v1.math.EtcException;
import io.github.artfultom.vecenta.generated.v1.math.FileNotFoundException;
import io.github.artfultom.vecenta.generated.v1.math.Model3;
import io.github.artfultom.vecenta.generated.v1.math.TestClient;
import io.github.artfultom.vecenta.generation.FileGenerator;
import io.github.artfultom.vecenta.generation.config.GenerateConfiguration;
import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.Connector;
import io.github.artfultom.vecenta.transport.MessageHandler;
import io.github.artfultom.vecenta.transport.Server;
import io.github.artfultom.vecenta.transport.error.ErrorType;
import io.github.artfultom.vecenta.transport.tcp.TcpConnector;
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
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ServerTest {

    @Test
    public void testGeneration() throws IOException, URISyntaxException {
        URL schemaRes = getClass().getResource("/schema_generation");
        assertNotNull(schemaRes);

        Path schemaDir = Path.of(schemaRes.toURI());
        Path tempDir = Files.createTempDirectory("test_" + System.currentTimeMillis());

        GenerateConfiguration config = new GenerateConfiguration(
                schemaDir,
                tempDir,
                "test.pack.model",
                "test.pack.exception",
                "test.pack.server",
                "test.pack.client"
        );

        Set<Path> files = new FileGenerator(config).generateFiles();
        assertEquals(9, files.size());

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
    public void testServer() throws URISyntaxException, IOException, ProtocolException, ConvertException, ConnectionException {
        URL res = getClass().getResource("/transfer_data");
        assertNotNull(res);

        String pack = "io.github.artfultom.vecenta.generated";
        GenerateConfiguration config = new GenerateConfiguration(
                Path.of(res.toURI()),
                Path.of("src", "test", "java"),
                pack,
                pack,
                pack,
                pack
        );

        Set<Path> files = new FileGenerator(config).generateFiles();
        assertNotNull(files);
        assertEquals(7, files.size());

        ServerMatcher matcher = new ServerMatcher();
        matcher.register(pack);

        try (Server server = new TcpServer(); Connector connector = new TcpConnector()) {
            int port = 5600;

            server.start(port, matcher);

            connector.connect("localhost", port);
            TestClient client = new TestClient(connector);

            int result1 = client.sum(3, 2);
            Assert.assertEquals(5, result1);

            String result2 = client.concat("test", "1", "2");
            Assert.assertEquals("test12", result2);

            Integer result3 = client.supply();
            Assert.assertNotNull(result3);
            Assert.assertEquals(42, result3.intValue());

            client.consume(42);

            Assert.assertThrows(RuntimeException.class, client::error1);

            Assert.assertThrows(FileNotFoundException.class, client::error2);

            Assert.assertThrows(EtcException.class, client::error3);
        }
    }

    @Test
    public void testServerEcho() throws URISyntaxException, IOException, ProtocolException, ConvertException, ConnectionException {
        URL res = getClass().getResource("/transfer_data");
        assertNotNull(res);

        String pack = "io.github.artfultom.vecenta.generated";
        GenerateConfiguration config = new GenerateConfiguration(
                Path.of(res.toURI()),
                Path.of("src", "test", "java"),
                pack,
                pack,
                pack,
                pack
        );

        Set<Path> files = new FileGenerator(config).generateFiles();
        assertNotNull(files);
        assertEquals(7, files.size());

        ServerMatcher matcher = new ServerMatcher();
        matcher.register(pack);

        try (Server server = new TcpServer(); Connector connector = new TcpConnector()) {
            int port = 5600;

            server.start(port, matcher);

            connector.connect("localhost", port);
            TestClient client = new TestClient(connector);

            Model3 model = new Model3();
            model.setField1(1);
            model.setField2((short) 2);
            model.setField3("test");
            model.setField4(true);

            int result1 = client.echo1(42);
            assertEquals(42, result1);

            List<Integer> result2 = client.echo2(List.of(1));
            assertEquals(List.of(1), result2);

            Model3 result3 = client.echo3(model);
            assertNotNull(result3);
            assertEquals(model.getField1(), result3.getField1());
            assertEquals(model.getField2(), result3.getField2());
            assertEquals(model.getField3(), result3.getField3());
            assertEquals(model.getField4(), result3.getField4());

            List<Model3> result4 = client.echo4(List.of(model));
            assertNotNull(result4);
            assertEquals(1, result4.size());
            assertEquals(model.getField1(), result4.get(0).getField1());
            assertEquals(model.getField2(), result4.get(0).getField2());
            assertEquals(model.getField3(), result4.get(0).getField3());
            assertEquals(model.getField4(), result4.get(0).getField4());

            Map<Integer, Model3> result5 = client.echo5(Map.of(1, model));
            assertNotNull(result5);
            assertEquals(1, result5.size());
            assertEquals(model.getField1(), result5.get(1).getField1());
            assertEquals(model.getField2(), result5.get(1).getField2());
            assertEquals(model.getField3(), result5.get(1).getField3());
            assertEquals(model.getField4(), result5.get(1).getField4());

            Map<Integer, List<Model3>> result6 = client.echo6(Map.of(1, List.of(model)));
            assertNotNull(result6);
            assertEquals(1, result6.size());
            assertEquals(1, result6.get(1).size());

            List<List<String>> result7 = client.echo7(List.of(List.of("TEST")));
            assertNotNull(result7);
            assertEquals(1, result7.size());
            assertEquals(1, result7.get(0).size());
            assertEquals("TEST", result7.get(0).get(0));

            Map<Integer, List<List<Model3>>> result8 = client.echo8(Map.of(1, List.of(List.of(model))));
            assertNotNull(result8);
            assertEquals(1, result8.size());
            assertEquals(1, result8.get(1).size());
            assertEquals(1, result8.get(1).get(0).size());

            Map<List<Model3>, List<List<Model3>>> result9 = client.echo9(Map.of(List.of(model), List.of(List.of(model))));
            assertNotNull(result9);
            assertEquals(1, result9.size());
        }
    }

    @Test
    public void testServerFail() throws ConvertException, ConnectionException {
        try (Server server = new TcpServer(); Connector connector = new TcpConnector()) {
            int port = 5601;

            server.start(port, new ServerMatcher());

            connector.connect("localhost", port);
            TestClient client = new TestClient(connector);
            client.sum(3, 2);

            Assert.fail("Must have an exception.");
        } catch (ProtocolException e) {
            Assert.assertEquals(ErrorType.WRONG_METHOD_NAME, e.getError());
        }
    }

    @Test
    public void testHandlers() throws ConnectionException, ProtocolException, ConvertException {
        ServerMatcher matcher = new ServerMatcher();
        String pack = "io.github.artfultom.vecenta.generated";
        matcher.register(pack);

        MessageHandler reverseHandler = bytes -> {
            byte[] result = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                result[i] = bytes[bytes.length - 1 - i];
            }

            return result;
        };

        try (Server server = new TcpServer(); Connector connector = new TcpConnector()) {
            int port = 5602;

            server.start(port, matcher);
            server.setGetHandler(reverseHandler);
            server.setSendHandler(reverseHandler);

            connector.connect("localhost", port);
            connector.setGetHandler(reverseHandler);
            connector.setSendHandler(reverseHandler);

            TestClient client = new TestClient(connector);
            int result = client.sum(3, 2);

            Assert.assertEquals(5, result);
        }
    }
}