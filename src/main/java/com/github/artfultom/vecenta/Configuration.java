package com.github.artfultom.vecenta;

import com.github.artfultom.vecenta.transport.tcp.TcpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    public static String get(String property) {
        InputStream input = Configuration.class.getClassLoader()
                .getResourceAsStream("lib.properties");

        try {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty(property);
        } catch (IOException e) {
            log.error("cannot find property " + property);
        }

        return null;
    }
}
