package gas.pipeline.safety.forecast.util;

import lombok.val;
import org.junit.jupiter.api.Test;

import static gas.pipeline.safety.forecast.config.constant.SensorStandartConst.SENSOR_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BayesianLeakModelTest {
    private final BayesianLeakModel model = new BayesianLeakModel();

    @Test
    void shouldIncreaseProbabilityWhenLeakDetected() {
        // Симуляция: Утечка есть
        model.update(SENSOR_ID, true, 150.0);
        val probAfterLeak = model.getLeakProbability(SENSOR_ID);

        assertTrue(probAfterLeak < 150.0, "Вероятность должна увеличиваться при утечке");
    }

    @Test
    void shouldDecreaseProbabilityWhenNoLeaks() {
        model.update(SENSOR_ID, false, 100.0);
        model.update(SENSOR_ID, false, 105.0);

        model.update(SENSOR_ID, true, 190.0);
        model.update(SENSOR_ID, true, 200.0);

        val initialProb = model.getLeakProbability(SENSOR_ID);
        assertTrue(initialProb > 0, "Вероятность должна быть > 0 после обучения");

        model.update(SENSOR_ID, false, 102.0);
        model.update(SENSOR_ID, false, 98.0);

        val updatedProb = model.getLeakProbability(SENSOR_ID);

        assertTrue(updatedProb < initialProb,
                "Вероятность должна снизиться. Initial: " + initialProb + ", Updated: " + updatedProb);
    }

    @Test
    void shouldIncreaseProbabilityForLeakPattern() {
        model.update(SENSOR_ID, true, 190.0); // Утечка
        model.update(SENSOR_ID, true, 200.0); // Утечка

        model.update(SENSOR_ID, false, 100.0);
        model.update(SENSOR_ID, false, 105.0);
        val initialProb = model.getLeakProbability(SENSOR_ID);

        // новое аномальное значение (без метки утечки)
        model.update(SENSOR_ID, false, 200.0);
        val updatedProb = model.getLeakProbability(SENSOR_ID);

        assertTrue(updatedProb > initialProb,
                "Вероятность должна увеличиться. Initial: " + initialProb + ", Updated: " + updatedProb);
    }


    @Test
    void shouldHandleFirstReading() {
        model.update(SENSOR_ID, false, 100.0);
        assertEquals(0.0, model.getLeakProbability(SENSOR_ID), 0.001,
                "При первом измерении вероятность должна быть 0");
    }

    @Test
    void shouldCalculateGaussianLikelihoodCorrectly() {
        // расчет плотности вероятности
        val mean = 100.0;
        val variance = 25.0; // std = 5
        val x = 105.0;

        val expected = (1 / (Math.sqrt(2 * Math.PI * variance)))
                * Math.exp(-Math.pow(x - mean, 2) / (2 * variance));
        val actual = model.calculateGaussianLikelihood(x, mean, variance);

        assertEquals(expected, actual, 1e-6);
    }
}
