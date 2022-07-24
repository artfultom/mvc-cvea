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
import io.github.artfultom.vecenta.generation.config.GenerateMode;
import io.github.artfultom.vecenta.matcher.ServerMatcher;
import io.github.artfultom.vecenta.transport.Connector;
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
            int port = 5550;

            server.start(port, matcher);

            connector.connect("127.0.0.1", port);
            TestClient client = new TestClient(connector);

            int result1 = client.sum(3, 2);
            Assert.assertEquals(5, result1);

            String result2 = client.concat("test", "1", "2");
            Assert.assertEquals("test12", result2);

            Model3 model = new Model3();
            model.setField1(1);
            model.setField2((short) 2);
            model.setField3("test");
            model.setField4(true);

            Model3 result3 = client.echo(model);
            Assert.assertEquals(model.getField1(), result3.getField1());
            Assert.assertEquals(model.getField2(), result3.getField2());
            Assert.assertEquals(model.getField3(), result3.getField3());
            Assert.assertEquals(model.getField4(), result3.getField4());

            List<Integer> list = List.of(1, 2, 3);
            List<Integer> result4 = client.echo(list);
            Assert.assertEquals(list, result4);

            List<Model3> result5 = client.echo(List.of(new Model3()), List.of(new Model3()));
            Assert.assertEquals(2, result5.size());

            Map<Integer, Model3> result6 = client.echo(Map.of(1, new Model3()));
            Assert.assertEquals(1, result6.size());

            Map<Integer, List<Model3>> result7 = client.echo(
                    Map.of(1, List.of(new Model3())),
                    Map.of(2, List.of(new Model3()))
            );
            Assert.assertEquals(2, result7.size());

            Integer result8 = client.supply();
            Assert.assertNotNull(result8);
            Assert.assertEquals(42, result8.intValue());

            client.consume(42);

            Assert.assertThrows(RuntimeException.class, client::error1);

            Assert.assertThrows(FileNotFoundException.class, client::error2);

            Assert.assertThrows(EtcException.class, client::error3);
        }
    }

    @Test
    public void testServerFail() throws URISyntaxException, IOException, ConvertException, ConnectionException {
        URL res = getClass().getResource("/transfer_data");
        assertNotNull(res);

        String pack = "io.github.artfultom.vecenta.generated";
        GenerateConfiguration config = new GenerateConfiguration(
                Path.of(res.toURI()),
                Path.of("src", "test", "java"),
                pack,
                pack,
                pack,
                pack,
                GenerateMode.CLIENT
        );

        Set<Path> files = new FileGenerator(config).generateFiles();
        assertNotNull(files);
        assertEquals(6, files.size());

        try (Server server = new TcpServer(); Connector connector = new TcpConnector()) {
            int port = 5550;

            server.start(port, new ServerMatcher());

            connector.connect("127.0.0.1", port);
            TestClient client = new TestClient(connector);
            client.sum(3, 2);

            Assert.fail("Must have an exception.");
        } catch (ProtocolException e) {
            Assert.assertEquals(ErrorType.WRONG_METHOD_NAME, e.getError());
        }
    }
}