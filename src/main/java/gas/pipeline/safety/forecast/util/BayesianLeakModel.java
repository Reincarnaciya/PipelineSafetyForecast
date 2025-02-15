package gas.pipeline.safety.forecast.util;

import lombok.val;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.HashMap;
import java.util.Map;

public class BayesianLeakModel {
    private final Map<String, Double> leakProbabilities = new HashMap<>();

    private final Map<String, Double> normalMean = new HashMap<>();
    private final Map<String, Double> normalVariance = new HashMap<>();
    private final Map<String, Integer> normalCount = new HashMap<>();

    private final Map<String, Double> leakMean = new HashMap<>();
    private final Map<String, Double> leakVariance = new HashMap<>();
    private final Map<String, Integer> leakCount = new HashMap<>();

    // Обновление вероятности при новом изменении
    public void update(String sensorId, boolean isLeak, double pressure, double temperature) {
        if (isLeak) {
            updateStats(sensorId, pressure, leakMean, leakVariance, leakCount);
        } else {
            updateStats(sensorId, pressure, normalMean, normalVariance, normalCount);
        }

        // Расчет вероятности утечки
        val prior = calculatePrior(sensorId);
        val likelihoodLeak = calculateGaussianLikelihood(
                pressure,
                leakMean.getOrDefault(sensorId, 0.0),
                leakVariance.getOrDefault(sensorId, 1.0)
        );

        val likelihoodNormal = calculateGaussianLikelihood(
                pressure,
                normalMean.getOrDefault(sensorId, 0.8),
                normalVariance.getOrDefault(sensorId, 1.0)
        );

        // Формула Байеса
        val posterior = (likelihoodLeak * prior) /
                (likelihoodLeak * prior + likelihoodNormal * (1 - prior));

        leakProbabilities.put(sensorId, posterior);
    }

    private void updateStats(String sensorId, double value,
                             Map<String, Double> meanMap,
                             Map<String, Double> varMap,
                             Map<String, Integer> countMap) {
        val count = countMap.getOrDefault(sensorId, 0);
        val oldMean = meanMap.getOrDefault(sensorId, 0.0);

        // Актуализация среднего по алгоритму Welford
        val newMean = oldMean + (value - oldMean) / (count + 1);
        meanMap.put(sensorId, newMean);

        // Актуализация дисперсии
        if (count > 0) {
            val oldVar = varMap.getOrDefault(sensorId, 0.0);
            val newVar = oldVar + (value - oldMean) * (value - newMean);
            varMap.put(sensorId, newVar);
        }
        countMap.put(sensorId, count + 1);
    }

    protected double calculateGaussianLikelihood(double x, double mean, double variance) {
        if (variance <= 0)
            variance = 1e-6;

        val dist = new NormalDistribution(mean, Math.sqrt(variance));
        return dist.density(x);
    }

    public double getLeakProbability(String sensorId) {
        if (leakCount.getOrDefault(sensorId, 0) < 2
                || normalCount.getOrDefault(sensorId, 0) < 2) {
            return 0.0; // Недостаточно данных
        }
        return leakProbabilities.getOrDefault(sensorId, 0.0);
    }

    private double calculatePrior(String sensorId) {
        val normal = normalCount.getOrDefault(sensorId, 0);
        val leak = leakCount.getOrDefault(sensorId, 0);
        val total = normal + leak;

        if (total == 0) return 0.01;
        return (double) leak / total;
    }
}
