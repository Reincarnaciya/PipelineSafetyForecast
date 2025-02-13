package gas.pipeline.safety.forecast.service;

import gas.pipeline.safety.forecast.model.SensorReading;
import gas.pipeline.safety.forecast.repository.SensorReadingRepository;
import gas.pipeline.safety.forecast.util.BayesianLeakModel;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

public class LeakDetectionServiceTest {
    private final SensorReadingRepository repoMock = Mockito.mock(SensorReadingRepository.class);
    private final BayesianLeakModel modelMock = Mockito.mock(BayesianLeakModel.class);
    private final LeakDetectionService service = new LeakDetectionService(repoMock, modelMock);

    @Test
    void shouIdSaveReadingAndUpdateModel() {
        val testReading = new SensorReading();
        testReading.setSensorId("sensor-001");
        testReading.setLeak(true);

        service.processNewReading(testReading);

        Mockito.verify(repoMock).save(testReading);
        Mockito.verify(modelMock).update(
            testReading.getSensorId(),
            testReading.isLeak(),
            testReading.getPressure(),
            testReading.getTemperature()
        );
    }

    @Test
    void shouldTriggerAlertWhenProbabilityExceedsThreshold() {
        Mockito.when(modelMock.getLeakProbability(any())).thenReturn(0.8);

        val testReading = new SensorReading();
        testReading.setSensorId("sensor-001");

        // Перехват потока вывода в консоль
        val outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        service.processNewReading(testReading);

        assertTrue(outContent.toString().contains("ALERT"));
        System.setOut(System.out);// Возращение стандартного вывода
    }
}
