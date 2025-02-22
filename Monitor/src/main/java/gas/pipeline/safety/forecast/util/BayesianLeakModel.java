package gas.pipeline.safety.forecast.util;

import lombok.val;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.HashMap;
import java.util.Map;

/**
 * Модель для байесовского прогнозирования утечек в газопроводах.
 * Использует данные с датчиков давления для вычисления апостериорной вероятности утечки.
 * Обновляет статистики (среднее, дисперсию) в реальном времени и применяет формулу Байеса.
 */
public class BayesianLeakModel {
    /**
     * Карта для хранения текущих вероятностей утечек по идентификаторам датчиков
     */
    private final Map<String, Double> leakProbabilities = new HashMap<>();

    // Статистики для нормального режима работы
    private final Map<String, Double> normalMean = new HashMap<>();
    private final Map<String, Double> normalVariance = new HashMap<>();
    private final Map<String, Integer> normalCount = new HashMap<>();

    // Статистики для режима утечки
    private final Map<String, Double> leakMean = new HashMap<>();
    private final Map<String, Double> leakVariance = new HashMap<>();
    private final Map<String, Integer> leakCount = new HashMap<>();


    /**
     * Обновляет статистики и пересчитывает вероятность утечки для датчика.
     *
     * @param sensorId идентификатор датчика
     * @param isLeak   флаг наличия утечки в текущем измерении
     * @param pressure значение давления с датчика
     */
    public void update(String sensorId, boolean isLeak, double pressure) {
        // Обновление статистик в зависимости от режима (утечка/норма)
        if (isLeak) {
            updateStats(sensorId, pressure, leakMean, leakVariance, leakCount);
        } else {
            updateStats(sensorId, pressure, normalMean, normalVariance, normalCount);
        }

        // Расчет апостериорной вероятности по формуле Байеса
        val prior = calculatePrior(sensorId); // априорная вероятность утечки
        val likelihoodLeak = calculateGaussianLikelihood(
                pressure,
                leakMean.getOrDefault(sensorId, 0.0), // если данных нет, используется 0.0
                leakVariance.getOrDefault(sensorId, 1.0) // предотвращение нулевой дисперсии
        );
        val likelihoodNormal = calculateGaussianLikelihood(
                pressure,
                normalMean.getOrDefault(sensorId, 0.8), // базовое давление в норме
                normalVariance.getOrDefault(sensorId, 1.0)
        );

        // Формула Байеса: P(Утечка|Данные) = (P(Данные|Утечка) * P(Утечка)) / P(Данные)
        val posterior = (likelihoodLeak * prior) /
                (likelihoodLeak * prior + likelihoodNormal * (1 - prior));

        leakProbabilities.put(sensorId, posterior);
    }

    /**
     * Обновляет статистики (среднее и дисперсию) для датчика с использованием алгоритма Уэлфорда.
     *
     * @param sensorId идентификатор датчика
     * @param value    текущее значение давления
     * @param meanMap  карта для хранения средних значений
     * @param varMap   карта для хранения дисперсий
     * @param countMap карта для хранения количества наблюдений
     */
    private void updateStats(String sensorId, double value,
                             Map<String, Double> meanMap,
                             Map<String, Double> varMap,
                             Map<String, Integer> countMap) {
        val count = countMap.getOrDefault(sensorId, 0);
        val oldMean = meanMap.getOrDefault(sensorId, 0.0);

        // Алгоритм Уэлфорда для инкрементального расчета среднего
        val newMean = oldMean + (value - oldMean) / (count + 1);
        meanMap.put(sensorId, newMean);

        // Инкрементальный расчет дисперсии (накапливаемая сумма квадратов отклонений)
        if (count > 0) {
            val oldVar = varMap.getOrDefault(sensorId, 0.0);
            val newVar = oldVar + (value - oldMean) * (value - newMean);
            varMap.put(sensorId, newVar);
        }
        countMap.put(sensorId, count + 1);
    }

    /**
     * Вычисляет вероятность значения по нормальному распределению.
     *
     * @param x        измеренное значение
     * @param mean     среднее распределения
     * @param variance дисперсия распределения
     * @return значение плотности вероятности (likelihood)
     */
    protected double calculateGaussianLikelihood(double x, double mean, double variance) {
        // Защита от нулевой или отрицательной дисперсии
        if (variance <= 0)
            variance = 1e-6;

        val dist = new NormalDistribution(mean, Math.sqrt(variance));
        return dist.density(x);
    }

    /**
     * Возвращает текущую вероятность утечки для датчика.
     *
     * @param sensorId идентификатор датчика
     * @return вероятность утечки (0.0, если данных недостаточно)
     */
    public double getLeakProbability(String sensorId) {
        // Требуется минимум 2 наблюдения для обеих статистик
        if (leakCount.getOrDefault(sensorId, 0) < 2
                || normalCount.getOrDefault(sensorId, 0) < 2) {
            return 0.0; // Недостаточно данных для надежной оценки
        }
        return leakProbabilities.getOrDefault(sensorId, 0.0);
    }

    /**
     * Вычисляет априорную вероятность утечки на основе исторических данных.
     *
     * @param sensorId идентификатор датчика
     * @return отношение числа утечек к общему количеству наблюдений (минимум 1%)
     */
    private double calculatePrior(String sensorId) {
        val normal = normalCount.getOrDefault(sensorId, 0);
        val leak = leakCount.getOrDefault(sensorId, 0);
        val total = normal + leak;

        // Возвращает 1% при отсутствии данных для избежания нулевых вероятностей
        if (total == 0) return 0.01;
        return (double) leak / total;
    }
}