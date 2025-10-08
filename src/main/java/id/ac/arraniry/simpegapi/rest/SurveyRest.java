package id.ac.arraniry.simpegapi.rest;

import id.ac.arraniry.simpegapi.entity.Survey;
import id.ac.arraniry.simpegapi.service.SurveyService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/survey")
public class SurveyRest {
    private final SurveyService surveyService;

    public SurveyRest(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping("/findToday")
    public Survey findToday() {
        return surveyService.findToday();
    }
}
