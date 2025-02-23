package gas.pipeline.safety.forecast.service.models;

import gas.pipeline.safety.forecast.config.ModelsConfig;
import gas.pipeline.safety.forecast.model.LeakPredictionCache;
import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.LeakPredictionCacheRepository;
import gas.pipeline.safety.forecast.repository.SensorReadingRepository;
import gas.pipeline.safety.forecast.util.BayesianLeakModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
public class LeakPredictionsService extends BaseLeakService {
    private final LeakPredictionCacheRepository predictionRepo;

    private final BayesianLeakModel leakModel;

    private final int defaultTrainingDays;
    private final int defaultPredictionsDays;
    private final int defaultAverageFrequency;


    @Autowired
    public LeakPredictionsService(SensorReadingRepository sensorReadingRepo,
                                  LeakPredictionCacheRepository predictionRepo,
                                  BayesianLeakModel leakModel,
                                  ModelsConfig modelsConfig) {
        super(sensorReadingRepo, modelsConfig);
        this.predictionRepo = predictionRepo;

        this.leakModel = leakModel;

        this.defaultTrainingDays = modelsConfig.getTrainingDays();
        this.defaultPredictionsDays = modelsConfig.getPredictionDays();
        this.defaultAverageFrequency = modelsConfig.getAverageFrequency();
    }

    @Override
    protected void processSensorReadings(String sensorId, List<SensorReading> data) {
        data.forEach(reading ->
                leakModel.update(
                        reading.getSensorId(),
                        reading.isLeak(),
                        reading.getPressure()
                )
        );
    }

    @PostConstruct
    public void initModel() {
        val startDate = LocalDateTime.now().minusDays(defaultTrainingDays);
        val sensorIds = sensorReadingRepo.findDistinctSensorIds();

        for (val sensorId : sensorIds) {
            val historicalData = sensorReadingRepo.findBySensorIdAndTimestampAfter(
                    sensorId,
                    startDate
            );
            for (val reading : historicalData) {
                leakModel.update(
                        reading.getSensorId(),
                        reading.isLeak(),
                        reading.getPressure()
                );
            }
        }

    }

    public void processNewReadings(SensorReading reading) {
        //sensorReadingRepo.save(reading);
        leakModel.update(reading.getSensorId(), reading.isLeak(), reading.getPressure());

        generatePredictions(reading.getSensorId());
        checkAlerts(reading.getSensorId());
    }


    void generatePredictions(String sensorId) {
        val frequency = calculateFrequency(sensorId);
        val totalPredictions = (int) (defaultPredictionsDays * frequency);

        for (int i = 1; i < totalPredictions + 1; i++) {
            val prediction = new LeakPredictionCache();
            prediction.setSensorId(sensorId);
            prediction.setTimestamp(LocalDateTime.now().plusMinutes(
                    (long) (i * (1440 / frequency)) // минуты в день / частоту
            ));
            prediction.setLeakProbability(leakModel.getLeakProbability(sensorId));
            predictionRepo.save(prediction);
        }
    }

    double calculateFrequency(String sensorId) {
        val opFirstTime = sensorReadingRepo.findFirstTimestamp();
        val opLastTime = sensorReadingRepo.findLastTimestamp();

        if (opFirstTime.isEmpty() || opLastTime.isEmpty()) {
            return 0.0;
        }
        val difference = ChronoUnit.DAYS.between(opFirstTime.get(), opLastTime.get());

        long trainingDays;
        if (difference > defaultAverageFrequency) {
            trainingDays = defaultTrainingDays;
        } else {
            trainingDays = difference;
        }

        val count = sensorReadingRepo.countBySensorIdAndTimestampAfter(
                sensorId,
                LocalDateTime.now().minusDays(trainingDays)
        );
        return count > 0 ? (double) count / trainingDays : defaultAverageFrequency;
    }


    private void checkAlerts(String sensorId) {
        val prob = leakModel.getLeakProbability(sensorId);
        if (prob > 0.7) {
            log.warn("Leak prediction probability is higher than {} for sensor {}", prob, sensorId);
        }
    }


}
