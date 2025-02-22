package gas.pipeline.safety.forecast.controller;


import gas.pipeline.safety.forecast.dto.SensorDTO;
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

@Slf4j
@RestController
@RequiredArgsConstructor
public class SensorRecord {
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
        } catch (Exception e) {
            ResponseEntity.badRequest().body(e.getMessage());
        }


        return ResponseEntity.ok("Request processed successfully");
    }
}
