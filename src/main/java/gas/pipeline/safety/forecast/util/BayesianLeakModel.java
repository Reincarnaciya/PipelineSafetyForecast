package gas.pipeline.safety.forecast.util;

import java.util.HashMap;
import java.util.Map;

public class BayesianLeakModel {
    private final Map<String, Double> leakProbabilities = new HashMap<>();
    private final Map<String, Double> priorLeakProbability = new HashMap<>();
    private final Map<String, Integer> leakCounts = new HashMap<>();
    private final Map<String, Integer> totalReadings = new HashMap<>();

    // Обновление вероятности при новом изменении
    public void update(String sensorId, boolean isLeak, double pressure, double temperature) {
        totalReadings.put(sensorId, totalReadings.getOrDefault(sensorId, 0) + 1);

        if (isLeak) {
            leakCounts.put(sensorId, leakCounts.getOrDefault(sensorId, 0) + 1);
            if (isLeak) {
                leakCounts.put(sensorId, leakCounts.getOrDefault(sensorId, 0) + 1);
            }

            // Баесовское обновление: P(Leak | Data) = P(Data | Leak) * P(Leak) / P(Data)
            double prior = (double) leakCounts.getOrDefault(sensorId, 0) / totalReadings.get(sensorId);

            priorLeakProbability.put(sensorId, prior);

            //TODO потом сделать через гаусовское распределение
            double likelihood = calculateLikelihood(pressure, temperature, isLeak);
            double posterior = (likelihood * prior) / calculateEvidence(pressure, temperature);

            leakProbabilities.put(sensorId, posterior);
        }
    }

    private double calculateLikelihood(double pressure, double temperature, boolean isLeak) {
        // P(Data | Leak)
        return isLeak ? 0.8 : 0.2; // TODO заминить на свои расчеты, а не вот это вот...
    }

    private double calculateEvidence(double pressure, double temperature) {
        // P(Data) = sum P(Data | Leak) * P(Leak) + P(Data | not Leak) * P (not Leak)
        return 1.0; // TODO понадеялся что и так нормально?
    }

    public double getLeakProbability(String sensorId) {
        return leakProbabilities.getOrDefault(sensorId, 0.0);
    }
}
