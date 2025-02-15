package gas.pipeline.safety.forecast.service;

import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.SensorReadingRepository;
import gas.pipeline.safety.forecast.util.BayesianLeakModel;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static gas.pipeline.safety.forecast.config.constant.SensorStandartConst.SENSOR_ID;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;

public class LeakDetectionServiceTest {
    private final SensorReadingRepository repoMock = Mockito.mock(SensorReadingRepository.class);
    private final BayesianLeakModel modelMock = Mockito.mock(BayesianLeakModel.class);
    private final LeakDetectionService service = new LeakDetectionService(repoMock, modelMock);

    @Test
    void shouldSaveReadingAndUpdateModel() {
        val testReading = new SensorReading();

        testReading.setSensorId(SENSOR_ID);
        testReading.setLeak(false);
        testReading.setPressure(100.0);
        testReading.setTemperature(20.0);

        service.processNewReading(testReading);
        Mockito.verify(repoMock).save(testReading);
        Mockito.verify(modelMock).update(
                eq(SENSOR_ID), // Используем константу
                eq(false),
                eq(100.0),
                eq(20.0)
        );
    }

    @Test
    void shouldTriggerAlertWhenProbabilityExceedsThreshold() {
        val testReading = new SensorReading();
        testReading.setSensorId(SENSOR_ID);
        testReading.setLeak(false);

        // Перехват потока вывода в консоль
        val outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        Mockito.when(modelMock.getLeakProbability(SENSOR_ID)).thenReturn(0.91);

        service.processNewReading(testReading);

        assertTrue(outContent.toString().contains(SENSOR_ID));
        System.setOut(System.out);// Возращение стандартного вывода
    }

    @Test
    void shouldNotTriggerAlertForConfirmedLeak() {
        val testReading = new SensorReading();
        testReading.setSensorId(SENSOR_ID);
        testReading.setLeak(true); // Подтвержденная утечка

        service.processNewReading(testReading);

        // Проверяем что алерт НЕ сработал
        Mockito.verify(modelMock, never()).getLeakProbability(any());
    }
}
