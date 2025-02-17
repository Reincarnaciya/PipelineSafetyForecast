package gas.pipeline.safety.forecast.service;

import gas.pipeline.safety.forecast.model.LeakPrediction;
import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.LeakPredictionRepository;
import gas.pipeline.safety.forecast.repository.SensorReadingRepository;
import gas.pipeline.safety.forecast.util.BayesianLeakModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LeakDetectionService {
    private final SensorReadingRepository sensorRepo;
    private final LeakPredictionRepository predictionRepo;

    private final BayesianLeakModel leakModel;

    @Autowired
    public LeakDetectionService(SensorReadingRepository sensorRepo,
                                LeakPredictionRepository predictionRepo) {
        this.sensorRepo = sensorRepo;
        this.predictionRepo = predictionRepo;
        this.leakModel = new BayesianLeakModel();
    }

    public void processNewReading(SensorReading reading) {
        sensorRepo.save(reading);
        leakModel.update(
                reading.getSensorId(),
                reading.isLeak(),
                reading.getPressure()
        );

        if (!reading.isLeak()) {
            LeakPrediction prediction = new LeakPrediction();
            prediction.setSensorId(reading.getSensorId());
            prediction.setTimestamp(LocalDateTime.now());
            prediction.setLeakProbability(leakModel.getLeakProbability(reading.getSensorId()));
            predictionRepo.save(prediction);
        }

        // Триггерим алерт только для неподтвержденных случаев
        if (!reading.isLeak() && leakModel.getLeakProbability(reading.getSensorId()) > 0.7) {
            System.out.println("ALERT: Potential leak detected in sensor " + reading.getSensorId());
        }
    }
}
