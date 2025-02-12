package gas.pipeline.safety.forecast;

import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.SensorReadingRepository;
import gas.pipeline.safety.forecast.service.LeakDetectionService;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@SpringBootApplication
public class PipelineSafetyForecastApplication {
    public static void main(String[] args) {
        val context = SpringApplication.run(PipelineSafetyForecastApplication.class, args);

        val service = context.getBean(LeakDetectionService.class);
        val repository = context.getBean(SensorReadingRepository.class);

        val reading = new SensorReading();
        reading.setSensorId("sensor-001");
        reading.setPressure(150.5);
        reading.setTemperature(25.0);
        reading.setLeak(false);
        reading.setTimestamp(LocalDateTime.now());

        service.processNewReading(reading);
    }
}
