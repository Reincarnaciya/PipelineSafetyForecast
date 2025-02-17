package gas.pipeline.safety.forecast.repository;

import gas.pipeline.safety.forecast.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
}

