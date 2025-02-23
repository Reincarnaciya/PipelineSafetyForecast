package gas.pipeline.safety.forecast.util;

import lombok.Data;
import lombok.val;

import java.util.HashMap;
import java.util.Map;


/**
 * Анализатор давления для обнаружения утечек в трубопроводах.
 * Использует статистические методы (Z-скор и CUSUM) для выявления аномалий в показаниях датчиков давления.
 * <p>
 * Основной алгоритм:
 * - Собирает статистику по первым 10 измерениям для каждого датчика (калибровка).
 * - Вычисляет отклонения последующих измерений от накопленной статистики.
 * - Срабатывает при превышении пороговых значений отклонения (Z-скор) или кумулятивной суммы (CUSUM).
 */
public class PressureAnalyzer {
    /**
     * Порог для кумулятивного суммирования отклонений (CUSUM)
     */
    private static final double CUSUM_THRESHOLD = 5.0;
    /**
     * Порог Z-скор для мгновенного обнаружения аномалий
     */
    private static final double LEAK_THRESHOLD = 3.0;

    // Статистика по каждому датчику: ключ - идентификатор датчика
    private final Map<String, SensorStats> sensorStats = new HashMap<>();

    /**
     * Анализирует текущее показание давления для указанного датчика.
     *
     * @param sensorId уникальный идентификатор датчика
     * @param pressure текущее значение давления
     * @return true - обнаружена утечка, false - аномалий нет
     */
    public boolean analyzePressure(String sensorId, double pressure) {
        // Получаем или создаем статистику для датчика
        val stats = sensorStats.computeIfAbsent(sensorId, k -> new SensorStats());

        // Калибровка: первые 10 измерений для накопления статистики
        if (stats.getCount() < 10) {
            updateStats(stats, pressure);
            return false;
        }

        // Проверка на аномалию
        val isLeak = checkAnomaly(stats, pressure);

        // Обновляем статистику только при нормальных показаниях
        if (!isLeak) {
            updateStats(stats, pressure);
        }
        return isLeak;
    }

    /**
     * Обновляет статистические показатели для датчика.
     * Используется алгоритм устойчивого вычисления среднего и дисперсии.
     *
     * @param stats объект статистики датчика
     * @param value новое значение давления
     */
    private void updateStats(SensorStats stats, double value) {
        val newCount = stats.getCount() + 1;
        // Вычисление дельты для инкрементального среднего
        val delta = value - stats.getMean();
        val newMean = stats.getMean() + delta / newCount;
        val newDelta = value - newMean;

        stats.setMean(newMean);
        // Обновление дисперсии методом Welford
        stats.setVariance(stats.getVariance() + delta * newDelta);
        stats.setCount(newCount);
    }

    /**
     * Проверяет показание на аномалию с использованием Z-скор и CUSUM.
     *
     * @param stats объект статистики датчика
     * @param value проверяемое значение давления
     * @return true - обнаружена аномалия, false - нормальное значение
     */
    private boolean checkAnomaly(SensorStats stats, double value) {
        // Расчет стандартного отклонения
        val stdDev = Math.sqrt(stats.getVariance() / stats.getCount());
        // Z-скор: количество сигм от среднего
        val zScore = Math.abs((value - stats.getMean()) / stdDev);

        // CUSUM: кумулятивная сумма отклонений с "дрейфом" 0.5 сигмы
        val cusum = Math.max(0, stats.getCusum() + (value - stats.getMean()) / stdDev - 0.5);
        stats.setCusum(cusum);

        // Условие срабатывания: превышение любого из порогов
        return zScore > LEAK_THRESHOLD || cusum > CUSUM_THRESHOLD;
    }

    public SensorStats getSensorStats(String sensorId) {
        return sensorStats.get(sensorId);
    }

    /**
     * Для хранения статистики датчика.
     */
    @Data
    public static class SensorStats {
        /**
         * Текущее среднее значение давления
         */
        private double mean;
        /**
         * Накопленная дисперсия
         */
        private double variance;
        /**
         * Количество учтенных измерений
         */
        private int count;
        /**
         * Текущее значение кумулятивной суммы отклонений
         */
        private double cusum;
    }
}
