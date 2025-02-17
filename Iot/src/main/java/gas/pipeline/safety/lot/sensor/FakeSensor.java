package gas.pipeline.safety.lot.sensor;


import lombok.val;

import java.util.Random;

public class FakeSensor extends Sensor {
    @Override
    public double getPressure() {
        val pressure = new Random().nextFloat(3.5f) + 9.0f;
        logger.info("Get fake pressure: " + pressure);
        return pressure;
    }
}
