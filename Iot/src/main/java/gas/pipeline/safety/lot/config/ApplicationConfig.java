package gas.pipeline.safety.lot.config;

import gas.pipeline.safety.lot.Main;
import lombok.val;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationConfig {
    public static final String SENSOR_ID;
    public static final Boolean SENSOR_REAL;

    public static final String CONNECT_URL;
    public static final Integer CONNECT_DELAY;
    public static final Integer CONNECT_MAX;

    public static final Integer SENDING_DELAY;

    static {
        val properties = new Properties();
        try (InputStream stream = Main.class.getResourceAsStream("/app.properties")) {
            properties.load(stream);

            SENSOR_ID = properties.getProperty("sensor.id");
            SENSOR_REAL = Boolean.parseBoolean(properties.getProperty("sensor.real"));

            CONNECT_URL = properties.getProperty("connect.url.path");
            CONNECT_DELAY = Integer.parseInt(properties.getProperty("connect.delay"));
            CONNECT_MAX = Integer.parseInt(properties.getProperty("connect.max"));
            SENDING_DELAY = Integer.parseInt(properties.getProperty("data.sending.delay"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
