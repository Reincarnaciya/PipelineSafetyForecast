package gas.pipeline.safety.forecast.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sensorId;
    private double pressure;
    private boolean isLeak;
    private LocalDateTime timestamp;


    public SensorReading(String sensorId, double pressure, boolean isLeak, LocalDateTime timestamp) {
        this.sensorId = sensorId;
        this.pressure = pressure;
        this.isLeak = isLeak;
        this.timestamp = timestamp;
    }
}
