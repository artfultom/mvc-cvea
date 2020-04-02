package my.artfultom.vecenta.controller;

import my.artfultom.vecenta.matcher.ServerMatcher;
import my.artfultom.vecenta.transport.Client;
import my.artfultom.vecenta.transport.Server;
import my.artfultom.vecenta.transport.message.Request;
import my.artfultom.vecenta.transport.message.Response;
import my.artfultom.vecenta.transport.tcp.TcpClient;
import my.artfultom.vecenta.transport.tcp.TcpServer;
import org.apache.avro.Schema;
import org.apache.avro.io.*;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumReader;
import org.apache.avro.reflect.ReflectDatumWriter;
import org.apache.avro.tool.Tool;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.util.List;

public class ControllerTest {

    @Test
    public void testGenerate() {

    }

    @Test
    public void testAvro() throws IOException {
        Schema schema = ReflectData.get().getSchema(A.class);
        BufferedWriter writer = new BufferedWriter(new FileWriter("./schema"));
        writer.write(schema.toString(true));
        writer.close();

        A a1 = new A(1, "qwe");

        DatumWriter<A> employeeWriter = new ReflectDatumWriter<>(A.class);
        byte[] data;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Encoder binaryEncoder = EncoderFactory.get().binaryEncoder(baos, null);
            employeeWriter.write(a1, binaryEncoder);
            binaryEncoder.flush();
            data = baos.toByteArray();
        }

        DatumReader<A> employeeReader = new ReflectDatumReader<>(A.class);
        Decoder binaryDecoder = DecoderFactory.get().binaryDecoder(data, null);

        A a2 = employeeReader.read(null, binaryDecoder);

        Assert.assertEquals(a1.getA(), a2.getA());
        Assert.assertEquals(a1.getB(), a2.getB());
    }

    @Test
    public void test() {
        ServerMatcher matcher = new ServerMatcher();
        matcher.register(ServerController.class); // TODO one or many?

        try (Server server = new TcpServer(); Client client = new TcpClient()) {
            server.start(5550, matcher);

            client.startConnection("127.0.0.1", 5550);
            ClientConnector clientConnector = new ClientConnector(client);
            int result = clientConnector.sum(3, 4);

            Assert.assertEquals(5, result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ClientConnector {
        private Client client;

        public ClientConnector(Client client) {
            this.client = client;
        }

        // TODO must be generated
        public Integer sum(Integer a, int b) {
            Request req = new Request(
                    "ServerController.sum(java.lang.Integer,int)",
                    List.of(ByteBuffer.allocate(4).putInt(2).array(), ByteBuffer.allocate(4).putInt(3).array())
            );
            Response resp = null;

            try {
                resp = client.send(req);
            } catch (ConnectException e) {
                e.printStackTrace();
                return null;
            }

            return ByteBuffer.wrap(resp.getResults().get(0)).getInt();
        }
    }

    public static class ServerController {

        public Integer sum(Integer a, int b) {
            return a + b;
        }
    }
}