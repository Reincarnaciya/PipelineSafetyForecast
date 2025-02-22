package gas.pipeline.safety.forecast.service.models;

import gas.pipeline.safety.forecast.config.ModelsConfig;
import gas.pipeline.safety.forecast.model.LeakPrediction;
import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.LeakPredictionRepository;
import gas.pipeline.safety.forecast.repository.SensorReadingRepository;
import gas.pipeline.safety.forecast.util.BayesianLeakModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class LeakPredictionsService extends BaseLeakService {
    private final LeakPredictionRepository predictionRepo;

    private final BayesianLeakModel leakModel;

    private final int defaultTrainingDays;
    private final int defaultPredictionsDays;
    private final int defaultAverageFrequency;


    @Autowired
    public LeakPredictionsService(SensorReadingRepository sensorReadingRepo,
                                  LeakPredictionRepository predictionRepo,
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
        sensorReadingRepo.save(reading);
        leakModel.update(reading.getSensorId(), reading.isLeak(), reading.getPressure());

        generatePredictions(reading.getSensorId());
        checkAlerts(reading.getSensorId());
    }


    void generatePredictions(String sensorId) {
        double frequency = calculateFrequency(sensorId);
        int totalPredictions = (int) (defaultPredictionsDays * frequency);

        for (int i = 0; i < totalPredictions; i++) {
            val prediction = new LeakPrediction();
            prediction.setSensorId(sensorId);
            prediction.setTimestamp(LocalDateTime.now().plusMinutes(
                    (long) (i * (1440 / frequency)) // минуты в день / частоту
            ));
            prediction.setLeakProbability(leakModel.getLeakProbability(sensorId));
            predictionRepo.save(prediction);
        }
    }

    double calculateFrequency(String sensorId) {
        val count = sensorReadingRepo.countBySensorIdAndTimestampAfter(
                sensorId,
                LocalDateTime.now().minusDays(defaultTrainingDays)
        );
        return count > 0 ? (double) count / defaultTrainingDays : defaultAverageFrequency;
    }


    private void checkAlerts(String sensorId) {
        double prob = leakModel.getLeakProbability(sensorId);
        if (prob > 0.7) {
            log.warn("Leak prediction probability is higher than {} for sensor {}", prob, sensorId);
        }
    }


}
