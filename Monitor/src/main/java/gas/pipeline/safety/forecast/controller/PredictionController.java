package gas.pipeline.safety.forecast.controller;

import gas.pipeline.safety.forecast.repository.LeakPredictionCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class PredictionController {
    private final LeakPredictionCacheRepository predictionRepo;

    @GetMapping("/predictions")
    public String getPredictions(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end,
            Model model
    ) {
        if (start == null) start = LocalDateTime.now().minusDays(1);
        if (end == null) end = LocalDateTime.now();

        val predictions = predictionRepo.findByPeriod(start, end);
        model.addAttribute("predictions", predictions);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        return "predictions";
    }
}
