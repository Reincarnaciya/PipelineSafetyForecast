package gas.pipeline.safety.forecast.controller;


import gas.pipeline.safety.forecast.dto.SensorDTO;
import gas.pipeline.safety.forecast.service.models.LeakDetectionService;
import gas.pipeline.safety.forecast.service.models.LeakPredictionsService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SensorRecordController {
    private final LeakPredictionsService leakPredictionsService;
    private final LeakDetectionService leakDetectionService;

    @PostMapping("/api/recording")
    public ResponseEntity<String> recording(@RequestBody SensorDTO sensor, HttpServletRequest request) {
        log.info("recording sensorId: {}, pressure: {}", sensor.getSensorId(), sensor.getPressure());
        val timestamp = request.getHeader("X-Request-Timestamp");
        if (timestamp == null) {
            return ResponseEntity.badRequest().body("X-Request-Timestamp is null");
        }
        try {
            val clientTime = LocalDateTime.parse(
                    timestamp,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );
            val reading = leakDetectionService.processSensorReading(
                    sensor.getSensorId(),
                    sensor.getPressure(),
                    clientTime
            );
            leakPredictionsService.processNewReadings(reading);
            val responseMassage = reading.isLeak()
                    ? "Leak detected! Sensor: " + sensor.getSensorId()
                    : "Request processed successfully";
            return ResponseEntity.ok(responseMassage);
        } catch (DateTimeParseException ex) {
            log.warn("X-Request-Timestamp is invalid: {}", timestamp);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
