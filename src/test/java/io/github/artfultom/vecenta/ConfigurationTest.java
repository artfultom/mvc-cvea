package io.github.artfultom.vecenta;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ConfigurationTest {

    @Test
    public void getInt() {
        int value = Configuration.getInt("send.attempt_count");
        assertEquals(10, value);
    }

    @Test
    public void getIntError() {
        try {
            Configuration.getInt("wrong_prop");
            fail();
        } catch (Exception e) {
            assertEquals("property wrong_prop not found", e.getMessage());
        }
    }

    @Test
    public void getLong() {
        long value = Configuration.getLong("server.first_client_id");
        assertEquals(0, value);
    }

    @Test
    public void getLongError() {
        try {
            Configuration.getLong("wrong_prop");
            fail();
        } catch (Exception e) {
            assertEquals("property wrong_prop not found", e.getMessage());
        }
    }

    @Test
    public void get() {
        String value = Configuration.get("send.attempt_count");
        assertEquals("10", value);
    }

    @Test
    public void getError() {
        try {
            Configuration.get("wrong_prop");
            fail();
        } catch (Exception e) {
            assertEquals("property wrong_prop not found", e.getMessage());
        }
    }
}