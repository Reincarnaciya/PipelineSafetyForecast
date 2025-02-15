package gas.pipeline.safety.forecast.util;

import lombok.val;
import org.junit.jupiter.api.Test;

import static gas.pipeline.safety.forecast.config.constant.SensorStandartConst.SENSOR_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BayesianLeakModelTest {
    private final BayesianLeakModel model = new BayesianLeakModel();

    private static final double LEAK_PRESSURE = 150.0;


    @Test
    void shouldIncreaseProbabilityWhenLeakDetected() {
        // Симуляция: Утечка есть
        model.update(SENSOR_ID, true, 150.0, 25.0);
        val probAfterLeak = model.getLeakProbability(SENSOR_ID);

        assertTrue(probAfterLeak < 150.0, "Вероятность должна увеличиваться при утечке");
    }

    @Test
    void shouldDecreaseProbabilityWhenNoLeaks() {
        // 1. Обучаем модель на примерах утечек И нормальных данных
        // Нормальные данные
        model.update(SENSOR_ID, false, 100.0, 20.0);
        model.update(SENSOR_ID, false, 105.0, 20.0);

        // Утечки
        model.update(SENSOR_ID, true, 190.0, 30.0);
        model.update(SENSOR_ID, true, 200.0, 35.0);

        // 2. Получаем начальную вероятность после обучения
        val initialProb = model.getLeakProbability(SENSOR_ID);
        assertTrue(initialProb > 0, "Вероятность должна быть > 0 после обучения");

        // 3. Подаем новые нормальные данные
        model.update(SENSOR_ID, false, 102.0, 21.0);
        model.update(SENSOR_ID, false, 98.0, 19.0);

        val updatedProb = model.getLeakProbability(SENSOR_ID);

        assertTrue(updatedProb < initialProb,
                "Вероятность должна снизиться. Initial: " + initialProb + ", Updated: " + updatedProb);
    }

    @Test
    void shouldIncreaseProbabilityForLeakPattern() {
        // Обучаем модель на примерах утечек
        model.update(SENSOR_ID, true, 190.0, 30.0); // Утечка
        model.update(SENSOR_ID, true, 200.0, 35.0); // Утечка

        // Обучаем на нормальных данных
        model.update(SENSOR_ID, false, 100.0, 20.0);
        model.update(SENSOR_ID, false, 105.0, 20.0);
        val initialProb = model.getLeakProbability(SENSOR_ID);

        // Подаем новое аномальное значение (без метки утечки)
        model.update(SENSOR_ID, false, 200.0, 25.0);
        val updatedProb = model.getLeakProbability(SENSOR_ID);

        assertTrue(updatedProb > initialProb,
                "Вероятность должна увеличиться. Initial: " + initialProb + ", Updated: " + updatedProb);
    }


    @Test
    void shouldHandleFirstReading() {
        model.update(SENSOR_ID, false, 100.0, 20.0);
        assertEquals(0.0, model.getLeakProbability(SENSOR_ID), 0.001,
                "При первом измерении вероятность должна быть 0");
    }

    @Test
    void shouldCalculateGaussianLikelihoodCorrectly() {
        // Тестируем расчет плотности вероятности
        val mean = 100.0;
        val variance = 25.0; // std = 5
        val x = 105.0;

        val expected = (1 / (Math.sqrt(2 * Math.PI * variance)))
                * Math.exp(-Math.pow(x - mean, 2) / (2 * variance));
        val actual = model.calculateGaussianLikelihood(x, mean, variance);

        assertEquals(expected, actual, 1e-6);
    }


    @Test
    void shouldUsePriorWhenDataIsScarce() {
        model.update(SENSOR_ID, false, 105.0, 25.0); // Всего 1 измерение

        val prob = model.getLeakProbability(SENSOR_ID);

        assertEquals(0.01, prob, 0.001,
                "При недостатке данных вероятность должна быть близка к априорной");
    }
}
