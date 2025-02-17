package gas.pipeline.safety.forecast.controller;


import gas.pipeline.safety.forecast.service.LeakDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SensorRecord {
    private final LeakDetectionService leakDetectionService;

    @PostMapping("/api/recording")
    public ResponseEntity<String> recording(
            @RequestParam String sensorId,
            @RequestParam Double pressure
    ) {
        log.info("recording sensorId: {}, pressure: {}", sensorId, pressure);
        return ResponseEntity.ok("Request processed successfully");
    }
}
