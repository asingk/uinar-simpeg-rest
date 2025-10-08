package id.ac.arraniry.simpegapi.service;

import id.ac.arraniry.simpegapi.dto.CreateResponse;
import id.ac.arraniry.simpegapi.entity.Survey;
import id.ac.arraniry.simpegapi.entity.SurveyResult;

public interface SurveyService {
    SurveyResult findTodayAnswerByNip(String nip);
    Survey findToday();
    CreateResponse answer(String nip, String surveyId, String answer);
}
