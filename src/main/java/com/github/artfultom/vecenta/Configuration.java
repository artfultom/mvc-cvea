package com.github.artfultom.vecenta;

import com.github.artfultom.vecenta.exceptions.PropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

    private static final Logger log = LoggerFactory.getLogger(Configuration.class);
    private static final String fileName = "lib.properties";

    public static String get(String property) {
        InputStream input = Configuration.class.getClassLoader()
                .getResourceAsStream(fileName);

        try {
            Properties prop = new Properties();
            prop.load(input);
            String value = prop.getProperty(property);

            if (value != null) {
                return value;
            }
        } catch (IOException e) {
            log.error("cannot find property " + property);
        }

        throw new PropertyNotFoundException(property);
    }

    public static int getInt(String property) {
        InputStream input = Configuration.class.getClassLoader()
                .getResourceAsStream(fileName);

        try {
            Properties prop = new Properties();
            prop.load(input);
            String value = prop.getProperty(property);

            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (IOException e) {
            log.error("cannot find property " + property);
        }

        throw new PropertyNotFoundException(property);
    }
}
