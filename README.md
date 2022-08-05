# vecenta

## Installation

```
repositories {
    mavenCentral()
}

dependencies {
    implementation 'io.github.artfultom:vecenta:0.0.7'
}
```

## Json-schema
An example:
```
./TestServer.1.json
```
The filename consists of **NAME_OF_SERVER.VERSION.json**. The generated server interface will have a name "TestServer.java". Then it must be implemented.

```
{
  "clients": [
    {
      "name": "TestClient",
      "entities": [
        {
          "name": "math",
          "methods": [
            {
              "name": "sum",
              "in": [
                {
                  "name": "a",
                  "type": "int32"
                },
                {
                  "name": "b",
                  "type": "int32"
                }
              ],
              "out": "int32"
            }
          ]
        }
      ]
    }
  ]
}
```
"TestClient.java" is a name of generated client class. Entity groups methods in the same package.

## Code generation.
You should use [vecenta-gradle-plugin](https://github.com/artfultom/vecenta-gradle-plugin).

```
plugins {
    id 'io.github.artfultom.vecenta.tools.vecenta-gradle-plugin' version '0.0.7'
}
```

```
generate {
    clientPackage = "test.client"                   // a package for generated client classes
    serverPackage = "test.server"                   // a package for generated server classes
    exceptionPackage = "test.exception"             // a package for generated exception classes
    modelPackage = "test.model"                     // a package for generated model classes
    schemaDir = "$projectDir/src/main/resources"    // a directory with json-schema
    targetDir = "$projectDir/src/main/java/"        // a directory for generated sources
}
```

```
./gradle generate
```

After a generation server interfaces must be implemented.

## Launch the server.
```
ServerMatcher matcher = new ServerMatcher();
matcher.register("test.server");  // package with server classes

try (Server server = new TcpServer()) {
  int port = 5550;
  server.start(port, matcher);
} catch (ConnectionException e) {
  ...
}
```

## Using of the client.
```
try (Connector connector = new TcpConnector()) {
  int port = 5550;
  connector.connect("127.0.0.1", port); // in this case ip is local
  TestClient client = new TestClient(connector);

  int result = client.sum(2, 3);  // method sum() will be executed on the server.
  System.out.println(result);
} catch (ConnectionException | ProtocolException | ConvertException e) {
  ...
}
```
