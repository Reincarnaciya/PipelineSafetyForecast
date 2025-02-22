package gas.pipeline.safety.forecast.service.models;


import gas.pipeline.safety.forecast.config.ModelsConfig;
import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.SensorReadingRepository;
import gas.pipeline.safety.forecast.util.PressureAnalyzer;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LeakDetectionService extends BaseLeakService {
    private final PressureAnalyzer pressureAnalyzer;

    @Autowired
    public LeakDetectionService(SensorReadingRepository sensorReadingRepository,
                                ModelsConfig modelsConfig, PressureAnalyzer pressureAnalyzer) {
        super(sensorReadingRepository, modelsConfig);
        this.pressureAnalyzer = pressureAnalyzer;
    }

    @Override
    protected void processSensorReadings(String sensorId, List<SensorReading> date) {
        date.stream()
                .filter(reading -> !reading.isLeak())
                .forEach(reading ->
                        pressureAnalyzer.analyzePressure(
                                reading.getSensorId(),
                                reading.getPressure()
                        )
                );
    }


    /*@PostConstruct
    public void init() {
        val endDate = LocalDateTime.now();
        val startDate = endDate.minusDays(modelsConfig.getTrainingDays());

        val sensorIds = sensorReadingRepository.findDistinctSensorIds();

        for (String sensorId : sensorIds) {
            List<SensorReading> trainingData = sensorReadingRepository
                    .findBySensorIdAndTimestampBetweenAndIsLeakFalse(
                            sensorId,
                            startDate,
                            endDate
                    );

            // Обучение модели на нормальных данных
            trainingData.forEach(reading ->
                    pressureAnalyzer.analyzePressure(
                            reading.getSensorId(),
                            reading.getPressure()
                    )
            );
        }
    }*/


    public SensorReading processSensorReading(String sensorId, double pressure, LocalDateTime timestamp) {
        val isLeak = pressureAnalyzer.analyzePressure(sensorId, pressure);
        val reading = new SensorReading(
                sensorId,
                pressure,
                isLeak,
                timestamp
        );
        return sensorReadingRepo.save(reading);
    }
}