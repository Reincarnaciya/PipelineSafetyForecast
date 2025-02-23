package gas.pipeline.safety.forecast.config;

import gas.pipeline.safety.forecast.util.PressureAnalyzer;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource(value = "classpath:config/model.properties")
@ConfigurationProperties(prefix = "pressure")
public class PressureModelsConfig {
    private double cusumThreshold;
    private double leakThreshold;
    private int calibrationRecords;


    @Bean
    public PressureAnalyzer pressureAnalyzer() {
        return new PressureAnalyzer(cusumThreshold, leakThreshold, calibrationRecords);
    }
}
