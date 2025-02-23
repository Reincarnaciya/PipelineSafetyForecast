package gas.pipeline.safety.forecast.repository;

import gas.pipeline.safety.forecast.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    @Query("SELECT s FROM SensorReading s ORDER BY s.timestamp DESC LIMIT :limit")
    List<SensorReading> findTopNByOrderByTimestampDesc(@Param("limit") int limit);

    List<SensorReading> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<SensorReading> findBySensorIdOrderByTimestampDesc(String sensorId);

    List<SensorReading> findBySensorIdAndTimestampAfter(String sensorId, LocalDateTime timestamp);

    List<SensorReading> findByTimestampAfter(LocalDateTime timestamp);

    long countBySensorIdAndTimestampAfter(String sensorId, LocalDateTime timestamp);

    @Query("SELECT DISTINCT s.sensorId FROM SensorReading s")
    List<String> findDistinctSensorIds();


    List<SensorReading> findBySensorIdAndTimestampBetweenAndIsLeakFalse(
            String sensorId,
            LocalDateTime start,
            LocalDateTime end
    );
}

