package gas.pipeline.safety.forecast.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BayesianLeakModelTest {
    private final BayesianLeakModel model = new BayesianLeakModel();
    private final String SENSOR_ID = "sensorId";

    @Test
    void shouldIncreaseProbabilityWhenLeakDetected() {
        // Симуляция: Утечка есть
        model.update(SENSOR_ID, true, 150.0, 25.0);
        double probAfterLeak = model.getLeakProbability(SENSOR_ID);

        assertTrue(probAfterLeak < 150.0, "Вероятность должна увеличиваться при утечке");
    }

    @Test
    void shouldDecreaseProbabilityWhenLeakDetected() {
        // Симуляция: 3 утечки подряд, затем 2 нормальных измерения
        for (int i = 0; i < 3; i++) {
            model.update(SENSOR_ID, true, 150.0, 25.0);
        }
        double initialProb = model.getLeakProbability(SENSOR_ID);

        for (int i = 0; i < 2; i++) {
            model.update(SENSOR_ID, false, 100.0, 20.0);
        }
        double updatedProb = model.getLeakProbability(SENSOR_ID);

        assertTrue(updatedProb < initialProb, "Вероятность должна снижаться без утечек");
    }

    @Test
    void shouldHandleFirstReading() {
        model.update(SENSOR_ID, false, 100.0, 20.0);
        assertEquals(0.0, model.getLeakProbability(SENSOR_ID), 0.001,
                "При первом измерении вероятность должна быть 0");
    }


}
