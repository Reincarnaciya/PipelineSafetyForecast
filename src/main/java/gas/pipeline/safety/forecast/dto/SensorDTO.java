package gas.pipeline.safety.forecast.dto;

import lombok.Data;

@Data
public class SensorDTO {
    String sensorId;
    double pressure;
    double temperature;
    boolean isLeak;
}
