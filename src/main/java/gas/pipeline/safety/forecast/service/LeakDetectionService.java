package gas.pipeline.safety.forecast.service;

import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.SensorReadingRepository;
import gas.pipeline.safety.forecast.util.BayesianLeakModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeakDetectionService {
    private final SensorReadingRepository repository;
    private final BayesianLeakModel leakModel;

    @Autowired
    public LeakDetectionService(SensorReadingRepository repository) {
        this.repository = repository;
        this.leakModel = new BayesianLeakModel();
    }


    public void processNewReading(SensorReading reading) {
        repository.save(reading);

        leakModel.update(
                reading.getSensorId(),
                reading.isLeak(),
                reading.getPressure(),
                reading.getTemperature()
        );

        if (leakModel.getLeakProbability(reading.getSensorId()) > 0.7) {
            System.out.println("ALERT: Potential leakk detected in sensor " + reading.getSensorId());
        }
    }
}
