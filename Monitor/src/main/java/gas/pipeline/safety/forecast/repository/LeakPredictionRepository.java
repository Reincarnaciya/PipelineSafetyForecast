package gas.pipeline.safety.forecast.repository;

import gas.pipeline.safety.forecast.model.LeakPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LeakPredictionRepository extends JpaRepository<LeakPrediction, Long> {
    @Query("SELECT p FROM LeakPrediction p WHERE p.timestamp BETWEEN :start AND :end")
    List<LeakPrediction> findByPeriod(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
