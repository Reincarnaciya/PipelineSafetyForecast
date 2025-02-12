package gas.pipeline.safety.forecast.repository;

import gas.pipeline.safety.forecast.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
}
