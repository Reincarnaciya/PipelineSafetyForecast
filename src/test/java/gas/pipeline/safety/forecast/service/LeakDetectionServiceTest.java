package gas.pipeline.safety.forecast.service;

import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.LeakPredictionRepository;
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
    private final SensorReadingRepository sensorRepoMock = Mockito.mock(SensorReadingRepository.class);
    private final LeakPredictionRepository predictionRepoMock = Mockito.mock(LeakPredictionRepository.class);

    private final BayesianLeakModel modelMock = Mockito.mock(BayesianLeakModel.class);

    private final LeakDetectionService service = new LeakDetectionService(sensorRepoMock, predictionRepoMock);

    @Test
    void shouldSaveReadingAndUpdateModel() {
        val testReading = new SensorReading();
        testReading.setSensorId(SENSOR_ID);
        testReading.setLeak(false);
        testReading.setPressure(100.0);

        service.processNewReading(testReading);

        // Проверка срхранения показаний
        Mockito.verify(sensorRepoMock).save(testReading);

        // Проверка обновления модели
        Mockito.verify(modelMock).update(
                eq(SENSOR_ID),
                eq(false),
                eq(100.0)
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

        assertTrue(outContent.toString().contains("ALERT"));
        assertTrue(outContent.toString().contains(SENSOR_ID));
        System.setOut(System.out);// Возращение стандартного вывода
    }

    @Test
    void shouldSavePredictionButNotTriggerAlertForConfirmedLeak() {
        val testReading = new SensorReading();
        testReading.setSensorId(SENSOR_ID);
        testReading.setLeak(true);

        service.processNewReading(testReading);

        // Прогноз должен сохраниться
        Mockito.verify(predictionRepoMock).save(any());

        // Но проверка вероятности не должна выполняться
        Mockito.verify(modelMock, never()).getLeakProbability(any());
    }

    @Test
    void shouldNotTriggerAlertBelowThreshold() {
        val testReading = new SensorReading();
        testReading.setSensorId(SENSOR_ID);
        testReading.setLeak(false);

        Mockito.when(modelMock.getLeakProbability(SENSOR_ID)).thenReturn(0.6);

        service.processNewReading(testReading);

        // Прогноз сохраняется в любом случае
        Mockito.verify(predictionRepoMock).save(any());

        // Нет вывода в консоль
        val outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        service.processNewReading(testReading);
        assertTrue(outContent.toString().isEmpty());
        System.setOut(System.out);
    }
}
